package com.lucas.aladin.utils

import io.jsonwebtoken.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret}") secret: String
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))

    fun validateToken(token: String?): Claims? {
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
        } catch (e: Exception) {
            null
        }
    }

    fun decodeToken(token: String?): Claims? {
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {
            e.claims
        }
    }

    fun generateToken(claims: Map<String, Any>): String {
        val now = Date()
        val expiry = Date(now.time + 1000L * 60 * 60 * 24 * 30)

        return Jwts.builder().claims(claims).issuedAt(now).expiration(expiry).signWith(secretKey).compact()
    }
}