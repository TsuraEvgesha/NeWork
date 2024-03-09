package ru.netology.nework.app.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.app.dto.Media
import ru.netology.nework.app.dto.MediaUpload
import ru.netology.nework.app.dto.Post
import ru.netology.nework.app.enumeration.AttachmentType


interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun save(post: Post)
    suspend fun likeById(id: Long)
    suspend fun saveWithAttachment(
        post: Post,
        upload: MediaUpload,
        type: AttachmentType
    )

    suspend fun dislikeById(id:Long)
    suspend fun removeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media

}