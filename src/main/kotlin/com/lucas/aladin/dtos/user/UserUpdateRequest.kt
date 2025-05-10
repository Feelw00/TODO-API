package com.lucas.aladin.dtos.user

data class UserUpdateRequest(
    val username: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)
