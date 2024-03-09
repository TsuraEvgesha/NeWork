package ru.netology.nework.app.dto

import java.io.InputStream

data class MediaUpload(
    val inputStream: InputStream,
)