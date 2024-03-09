package ru.netology.nework.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.app.dao.EventDao
import ru.netology.nework.app.dao.EventRemoteKeyDao
import ru.netology.nework.app.dao.JobDao
import ru.netology.nework.app.dao.PostDao
import ru.netology.nework.app.dao.PostRemoteKeyDao
import ru.netology.nework.app.dao.UserDao
import ru.netology.nework.app.entity.EventEntity
import ru.netology.nework.app.entity.EventRemoteKeyEntity
import ru.netology.nework.app.entity.JobEntity
import ru.netology.nework.app.entity.PostEntity
import ru.netology.nework.app.entity.PostRemoteKeyEntity
import ru.netology.nework.app.entity.UserEntity
import ru.netology.nework.app.utils.Converters



@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        UserEntity::class,
        JobEntity::class,
    ],
    version = 11,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
}