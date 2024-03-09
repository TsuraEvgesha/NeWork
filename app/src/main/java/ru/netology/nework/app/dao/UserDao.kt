package ru.netology.nework.app.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.app.entity.UserEntity


@Dao
interface UserDao {

    @Query("SELECT * FROM UserEntity ORDER BY name ASC")
    fun getAll(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(users: List<UserEntity>)

    @Query("DELETE FROM UserEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT * FROM UserEntity WHERE id LIKE :searchQuery OR name LIKE :searchQuery")
    fun searchUser(searchQuery: String): Flow<List<UserEntity>>
}