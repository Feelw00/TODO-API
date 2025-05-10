package com.lucas.aladin.dtos.todo

import java.time.OffsetDateTime

data class TodoCreateRequest(
    val title: String,
    val description: String? = null,
    val dueDate: OffsetDateTime? = null
)