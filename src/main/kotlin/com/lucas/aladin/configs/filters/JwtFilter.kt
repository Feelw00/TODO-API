package com.lucas.aladin.configs.filters

import com.lucas.aladin.dtos.auth.AuthenticatedUser
import com.lucas.aladin.utils.JwtUtil
import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            val claims: Claims? = jwtUtil.validateToken(token)

            if (claims != null && SecurityContextHolder.getContext().authentication == null) {
                val userId = (claims["id"] as? Number)?.toLong()
                val email = claims["email"] as? String
                val username = claims["username"] as? String


                if (userId != null && !email.isNullOrBlank() && !username.isNullOrBlank()) {
                    val principal = AuthenticatedUser(userId, email, username)

                    val auth = UsernamePasswordAuthenticationToken(
                        principal, // ✅ 올바른 principal
                        null,
                        emptyList() // 권한은 나중에 추가 가능
                    )
                    auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
