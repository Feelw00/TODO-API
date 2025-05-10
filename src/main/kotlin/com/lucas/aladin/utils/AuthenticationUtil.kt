package com.lucas.aladin.utils

import com.lucas.aladin.dtos.auth.AuthenticatedUser
import org.springframework.security.core.context.SecurityContextHolder

object AuthenticationUtil {
    fun getAuthenticatedUser(): AuthenticatedUser {
        return SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
    }
}
