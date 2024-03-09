package ru.netology.nework.app.model

import android.net.Uri
import ru.netology.nework.app.enumeration.AttachmentType


import java.io.InputStream

data class MediaModel(
    val uri: Uri? = null,
    val inputStream: InputStream? = null,
    val type: AttachmentType? = null,
)