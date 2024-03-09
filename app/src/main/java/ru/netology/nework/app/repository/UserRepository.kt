package ru.netology.nework.app.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.app.dto.User

interface UserRepository {

    val data: Flow<List<User>>

    suspend fun getAll()
    fun searchUser(searchQuery: String): Flow<List<User>>
}