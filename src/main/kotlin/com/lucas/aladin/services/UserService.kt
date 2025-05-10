package com.lucas.aladin.services

import com.lucas.aladin.configs.exceptions.*
import com.lucas.aladin.dtos.auth.LoginResponse
import com.lucas.aladin.dtos.user.*
import com.lucas.aladin.mappers.UserMapper
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.utils.JwtUtil
import com.lucas.aladin.utils.PasswordUtil
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordUtil: PasswordUtil,
    private val jwtUtil: JwtUtil
) {

    @Transactional
    fun signup(request: UserSignupRequest): LoginResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(request.email)
        }
        request.password = passwordUtil.encode(request.password)

        val user = userMapper.signupRequestToEntity(request)
        val savedUser = userRepository.save(user)

        val claims = mapOf("id" to savedUser.id!!, "email" to savedUser.email, "username" to savedUser.username)
        val accessToken = jwtUtil.generateToken(claims)
        return LoginResponse(accessToken)
    }

    fun login(request: UserLoginRequest): LoginResponse {
        val user = userRepository.findFirstByEmail(request.email) ?: throw EmailNotFoundException(request.email)

        if (!passwordUtil.matches(request.password, user.passwordHash)) {
            throw InvalidPasswordException()
        }

        val claims = mapOf("id" to user.id!!, "email" to user.email, "username" to user.username)
        val accessToken = jwtUtil.generateToken(claims)

        return LoginResponse(accessToken)
    }

    fun getMe(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException() }
        return userMapper.userToResponse(user)
    }

    @Transactional
    fun updateMe(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException() }
        if (request.username == null && request.newPassword == null) {
            throw NoUpdatableFieldException()
        }

        request.username?.let {
            user.username = it
        }

        if (request.newPassword != null) {
            val current = request.currentPassword ?: throw InvalidPasswordException("현재 비밀번호가 필요합니다")

            if (!passwordUtil.matches(current, user.passwordHash)) {
                throw InvalidPasswordException("현재 비밀번호가 일치하지 않습니다")
            }

            user.passwordHash = passwordUtil.encode(request.newPassword)
        }

        return userMapper.userToResponse(userRepository.save(user))
    }

    @Transactional
    fun deleteMe(id: Long) {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException() }
        userRepository.delete(user)
    }
}