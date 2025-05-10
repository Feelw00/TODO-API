package com.lucas.aladin

import com.lucas.aladin.services.UserService
import com.lucas.aladin.utils.JwtUtil
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeIn
import io.mockk.clearMocks
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc, private val jwtUtil: JwtUtil
) : ShouldSpec({

    val validToken = jwtUtil.generateToken(mapOf("id" to 1L, "email" to "social@aladin.com", "username" to "mock-user"))
    val invalidToken = "Bearer this.is.invalid.token"
    val userService = mockk<UserService>()

    beforeTest {
        clearMocks(userService)
    }

    context("JWT 인증 테스트") {
        should("JWT 없이 /users/me 접근 시 401 반환") {
            mockMvc.get("/users/me").andExpect {
                status { isUnauthorized() }
            }
        }

        should("잘못된 JWT로 /users/me 접근 시 401 반환") {
            mockMvc.get("/users/me") {
                header(HttpHeaders.AUTHORIZATION, invalidToken)
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        should("정상 JWT로 /users/me 접근 시 인증 통과 → 200 또는 404 반환") {
            val result = mockMvc.get("/users/me") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
            }.andReturn()

            result.response.status shouldBeIn listOf(200, 404)
        }
    }
})