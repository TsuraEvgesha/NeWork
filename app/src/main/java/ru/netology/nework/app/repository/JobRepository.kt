package ru.netology.nework.app.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.app.dto.Job


interface JobRepository {

    val data: Flow<List<Job>>

    suspend fun getAuthJobs()
    suspend fun getJobByUserId(id: Long)
    suspend fun removeById(id: Long)
    suspend fun editJob(job: Job)

}