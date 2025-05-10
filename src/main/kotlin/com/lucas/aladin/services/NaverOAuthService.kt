package com.lucas.aladin.services

import com.lucas.aladin.dtos.auth.LoginResponse
import com.lucas.aladin.dtos.auth.NaverTokenResponse
import com.lucas.aladin.dtos.auth.NaverUserInfo
import com.lucas.aladin.dtos.auth.NaverUserResponse
import com.lucas.aladin.entities.User
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.utils.JwtUtil
import com.lucas.aladin.utils.PasswordUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class NaverOAuthService(
    @Value("\${spring.security.oauth2.client.registration.naver.client-id}") private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.naver.client-secret}") private val clientSecret: String,
    @Value("\${spring.security.oauth2.client.registration.naver.redirect-uri}") private val redirectUri: String,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val passwordUtil: PasswordUtil
) {
    private val restTemplate = RestTemplate()

    fun login(code: String, state: String): LoginResponse {
        val tokenResponse = getToken(code, state)
        val userInfo = getUserInfo(tokenResponse.accessToken)

        val naverId = userInfo.id
        val email = userInfo.email
        val username = userInfo.name

        val user = userRepository.findFirstByEmail(email)
            ?: userRepository.save(
                User(
                    username = username,
                    email = email,
                    passwordHash = passwordUtil.encode("social:naver:$naverId")
                )
            )

        val claims = mapOf(
            "id" to user.id!!,
            "email" to user.email,
            "username" to user.username
        )

        return LoginResponse(
            accessToken = jwtUtil.generateToken(claims),
        )
    }

    private fun getToken(code: String, state: String): NaverTokenResponse {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("code", code)
            add("state", state)
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val response = restTemplate.postForEntity(
            "https://nid.naver.com/oauth2.0/token",
            HttpEntity(params, headers),
            NaverTokenResponse::class.java
        )

        return response.body ?: throw IllegalStateException("네이버 토큰 응답 없음")
    }

    private fun getUserInfo(accessToken: String): NaverUserInfo {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }

        val response = restTemplate.exchange(
            "https://openapi.naver.com/v1/nid/me",
            HttpMethod.GET,
            HttpEntity(null, headers),
            NaverUserResponse::class.java
        )

        return response.body?.response ?: throw IllegalStateException("네이버 사용자 정보 없음")
    }
}
