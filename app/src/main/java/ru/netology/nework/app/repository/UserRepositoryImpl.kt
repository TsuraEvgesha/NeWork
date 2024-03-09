package ru.netology.nework.app.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.app.api.UserApiService
import ru.netology.nework.app.dao.UserDao
import ru.netology.nework.app.dto.User
import ru.netology.nework.app.entity.toDto
import ru.netology.nework.app.entity.toUserEntity
import ru.netology.nework.app.errors.ApiError
import ru.netology.nework.app.errors.NetworkError
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApiService: UserApiService,
) : UserRepository {

    override val data: Flow<List<User>> =
        userDao.getAll()
            .map { it.toDto() }
            .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            userDao.getAll()
            val response = userApiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            userDao.insert(body.toUserEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }
    override fun searchUser(searchQuery: String): Flow<List<User>> {
        return userDao.searchUser(searchQuery).map {
            it.toDto()
        }.flowOn(Dispatchers.Default)
    }
}