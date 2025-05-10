package com.lucas.aladin.dtos.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String
)

data class KakaoUserResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
) {
    val email: String get() = kakaoAccount.email
    val nickname: String get() = kakaoAccount.profile.nickname
}

data class KakaoAccount(
    val email: String,
    val profile: KakaoProfile
)

data class KakaoProfile(
    val nickname: String
)

data class OAuthLoginRequest(
    val code: String
)