package com.lucas.aladin.utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordUtil(
    private val encoder: PasswordEncoder = BCryptPasswordEncoder()
) {
    fun encode(rawPassword: String): String {
        return encoder.encode(rawPassword)
    }

    fun matches(rawPassword: String, hashedPassword: String): Boolean {
        return encoder.matches(rawPassword, hashedPassword)
    }
}