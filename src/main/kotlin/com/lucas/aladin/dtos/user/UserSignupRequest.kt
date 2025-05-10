package com.lucas.aladin.dtos.user

data class UserSignupRequest(
    val username: String,
    val email: String,
    var password: String,
)
