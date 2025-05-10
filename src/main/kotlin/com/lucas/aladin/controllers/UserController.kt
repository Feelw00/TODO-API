package com.lucas.aladin.controllers

import com.lucas.aladin.dtos.auth.LoginResponse
import com.lucas.aladin.dtos.user.*
import com.lucas.aladin.services.UserService
import com.lucas.aladin.utils.AuthenticationUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/signup")
    fun signup(@RequestBody userSignupRequest: UserSignupRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(userService.signup(userSignupRequest))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(userService.login(request))
    }

    @GetMapping("/me")
    fun getMe(): UserResponse {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        return userService.getMe(authUser.id)
    }

    @PutMapping("/me")
    fun updateMe(@RequestBody request: UserUpdateRequest): UserResponse {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        return userService.updateMe(authUser.id, request)
    }

    @DeleteMapping("/me")
    fun deleteMe(): ResponseEntity<Void> {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        userService.deleteMe(authUser.id)
        return ResponseEntity.noContent().build()
    }
}