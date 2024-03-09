package ru.netology.nework.app.dto

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)