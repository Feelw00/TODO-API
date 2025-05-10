package com.lucas.aladin.dtos.todo

import java.time.OffsetDateTime

data class TodoUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val dueDate: OffsetDateTime? = null,
    val completed: Boolean? = null
)
