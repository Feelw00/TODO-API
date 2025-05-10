package com.lucas.aladin.controllers

import com.lucas.aladin.dtos.auth.LoginResponse
import com.lucas.aladin.dtos.auth.OAuthLoginRequest
import com.lucas.aladin.dtos.auth.OAuthLoginRequestWithState
import com.lucas.aladin.services.KakaoOAuthService
import com.lucas.aladin.services.NaverOAuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class OAuthController(
    private val kakaoOAuthService: KakaoOAuthService,
    private val naverOAuthService: NaverOAuthService,
) {

    @PostMapping("/login/kakao")
    fun kakaoLogin(@RequestBody request: OAuthLoginRequest): ResponseEntity<LoginResponse> {
        val response = kakaoOAuthService.login(request.code)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login/naver")
    fun naverLogin(@RequestBody request: OAuthLoginRequestWithState): ResponseEntity<LoginResponse> {
        val response = naverOAuthService.login(request.code, request.state)
        return ResponseEntity.ok(response)
    }
}