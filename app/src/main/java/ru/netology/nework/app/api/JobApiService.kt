package ru.netology.nework.app.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.app.dto.Job


interface JobApiService {


    @GET("my/jobs")
    suspend fun getOwnJobs(): Response<List<Job>>

    @GET("{id}/jobs")
    suspend fun getJobByUserId(@Path("id") id: Long): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>
}