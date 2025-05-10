package com.lucas.aladin.dtos.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverTokenResponse(
    @JsonProperty("access_token") val accessToken: String
)

data class NaverUserResponse(
    val response: NaverUserInfo
)

data class NaverUserInfo(
    val id: String,
    val email: String,
    val name: String
)

data class OAuthLoginRequestWithState(
    val code: String,
    val state: String
)