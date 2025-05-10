package com.lucas.aladin.services

import com.lucas.aladin.dtos.auth.KakaoTokenResponse
import com.lucas.aladin.dtos.auth.KakaoUserResponse
import com.lucas.aladin.dtos.auth.LoginResponse
import com.lucas.aladin.entities.User
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.utils.JwtUtil
import com.lucas.aladin.utils.PasswordUtil
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class KakaoOAuthService(
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}") private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.kakao.redirect-uri}") private val redirectUri: String,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val passwordUtil: PasswordUtil
) {
    private val restTemplate = RestTemplate()

    @Transactional
    fun login(code: String): LoginResponse {
        val tokenResponse = getToken(code)
        val userInfo = getUserInfo(tokenResponse.accessToken)

        val kakaoId = userInfo.id.toString()
        val email = userInfo.email
        val username = userInfo.nickname

        val user = userRepository.findFirstByEmail(email) ?: userRepository.save(
            User(
                username = username,
                email = email,
                passwordHash = passwordUtil.encode("social:kakao:$kakaoId")
            )
        )

        val claims = mapOf(
            "id" to user.id!!, "email" to user.email, "username" to user.username
        )
        val accessToken = jwtUtil.generateToken(claims)
        return LoginResponse(
            accessToken = accessToken,
        )
    }

    private fun getToken(code: String): KakaoTokenResponse {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("redirect_uri", redirectUri)
            add("code", code)
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val request = HttpEntity(params, headers)

        val response = restTemplate.postForEntity(
            "https://kauth.kakao.com/oauth/token", request, KakaoTokenResponse::class.java
        )

        return response.body ?: throw IllegalStateException("카카오 토큰 응답이 없습니다.")
    }

    private fun getUserInfo(accessToken: String): KakaoUserResponse {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }

        val request = HttpEntity(null, headers)

        val response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, KakaoUserResponse::class.java
        )

        return response.body ?: throw IllegalStateException("카카오 사용자 정보가 없습니다.")
    }
}
