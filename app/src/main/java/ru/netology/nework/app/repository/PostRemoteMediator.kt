package ru.netology.nework.app.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.app.api.PostApiService
import ru.netology.nework.app.dao.PostDao
import ru.netology.nework.app.dao.PostRemoteKeyDao
import ru.netology.nework.app.db.AppDb
import ru.netology.nework.app.entity.PostEntity
import ru.netology.nework.app.entity.PostRemoteKeyEntity
import ru.netology.nework.app.errors.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val postDao: PostDao,
    private val postApiService: PostApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                REFRESH -> {
                    postApiService.getPostLatest(state.config.pageSize)
                }
                APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    postApiService.getPostBefore(id, state.config.pageSize)
                }
                PREPEND -> {
                    return MediatorResult.Success(true)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.message())
            }

            if (result.body().isNullOrEmpty())
                return MediatorResult.Success(true)

            val body = result.body() ?: throw ApiError(result.message())

            appDb.withTransaction {
                when (loadType) {
                    REFRESH -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )

                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                        postDao.removeAll()
                    }

                    APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            ),
                        )
                    }

                    PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                        )
                    }
                }
                postDao.insertPosts(body.map(PostEntity::fromDto))
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}