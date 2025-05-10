package com.lucas.aladin.dtos.user

data class UserLoginRequest(
    val email: String,
    val password: String,
)