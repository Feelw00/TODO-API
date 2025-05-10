package com.lucas.aladin.dtos.todo

import java.time.OffsetDateTime

data class TodoResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val dueDate: OffsetDateTime?,
    val completed: Boolean,
    val createdAt: OffsetDateTime
)
