package ru.netology.nework.app.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.app.dto.Post

interface WallRepository {

    fun loadUserWall(userId: Long): Flow<PagingData<Post>>
}