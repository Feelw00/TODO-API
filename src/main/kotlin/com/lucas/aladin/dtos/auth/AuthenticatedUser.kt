package com.lucas.aladin.dtos.auth

import io.jsonwebtoken.Claims

data class AuthenticatedUser(
    val id: Long,
    val email: String,
    val username: String,
) {
    companion object {
        fun fromClaims(claims: Claims): AuthenticatedUser {
            return AuthenticatedUser(
                claims["id"] as Long,
                claims["email"] as String,
                claims["username"] as String,
            )
        }
    }
}