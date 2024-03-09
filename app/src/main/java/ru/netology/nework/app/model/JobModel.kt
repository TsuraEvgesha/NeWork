package ru.netology.nework.app.model

import ru.netology.nework.app.dto.Job

data class JobModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false,
)