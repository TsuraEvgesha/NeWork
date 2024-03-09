package ru.netology.nework.app.dto

import ru.netology.nework.app.enumeration.AttachmentType


data class Attachment (val url: String, val type: AttachmentType)