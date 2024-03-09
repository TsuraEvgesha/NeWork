package ru.netology.nework.app.entity

import ru.netology.nework.app.dto.Attachment
import ru.netology.nework.app.enumeration.AttachmentType


data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}