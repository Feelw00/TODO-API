package com.lucas.aladin.dtos.todo

import java.time.OffsetDateTime

data class TodoSearchRequest(
    val title: String? = null,
    val completed: Boolean? = null,
    val dueDateFrom: OffsetDateTime? = null,
    val dueDateTo: OffsetDateTime? = null
)