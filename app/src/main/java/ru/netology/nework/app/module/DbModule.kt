package ru.netology.nework.app.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.app.db.AppDb
import ru.netology.nework.app.dao.*

import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context,
    ): AppDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providePostDao(
        appDb: AppDb,
    ): PostDao = appDb.postDao()

    @Provides
    fun providePostRemoteKeyDao(
        appDb: AppDb,
    ): PostRemoteKeyDao = appDb.postRemoteKeyDao()

    @Provides
    fun provideEventDao(
        appDb: AppDb,
    ): EventDao = appDb.eventDao()

    @Provides
    fun provideEventRemoteKeyDao(
        appDb: AppDb,
    ): EventRemoteKeyDao = appDb.eventRemoteKeyDao()

    @Provides
    fun provideUserDao(
        appDb: AppDb,
    ): UserDao = appDb.userDao()

    @Provides
    fun provideJobDao(
        appDb: AppDb,
    ): JobDao = appDb.jobDao()
}