package com.lucas.aladin

import com.lucas.aladin.dtos.auth.*
import com.lucas.aladin.entities.User
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.services.NaverOAuthService
import com.lucas.aladin.utils.JwtUtil
import com.lucas.aladin.utils.PasswordUtil
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate

@SpringBootTest
@ActiveProfiles("test")
class NaverOAuthServiceTest : BehaviorSpec({

    val restTemplate = mockk<RestTemplate>()
    val userRepository = mockk<UserRepository>()
    val jwtUtil = mockk<JwtUtil>()
    val passwordUtil = mockk<PasswordUtil>()

    val clientId = "test-client-id"
    val clientSecret = "test-client-secret"
    val redirectUri = "http://localhost/auth/naver/callback"

    val service = NaverOAuthService(
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUri = redirectUri,
        userRepository = userRepository,
        jwtUtil = jwtUtil,
        passwordUtil = passwordUtil
    ).apply {
        val field = NaverOAuthService::class.java.getDeclaredField("restTemplate")
        field.isAccessible = true
        field.set(this, restTemplate)
    }

    val code = "mock-code"
    val state = "mock-state"
    val accessToken = "naver-access-token"

    val email = "user@naver.com"
    val name = "네이버사용자"
    val naverId = "naver-id-123"

    val tokenResponse = NaverTokenResponse(accessToken = accessToken)
    val userResponse = NaverUserResponse(
        response = NaverUserInfo(id = naverId, email = email, name = name)
    )

    val jwt = "jwt-token"

    beforeTest {
        clearAllMocks()
    }

    given("네이버 인가 코드와 state를 전달했을 때") {

        When("유저가 존재하지 않으면") {
            Then("회원가입 후 JWT를 발급한다") {
                every {
                    restTemplate.postForEntity(
                        "https://nid.naver.com/oauth2.0/token", any(), NaverTokenResponse::class.java
                    )
                } returns ResponseEntity.ok(tokenResponse)

                every {
                    restTemplate.exchange(
                        "https://openapi.naver.com/v1/nid/me", HttpMethod.GET, any(), NaverUserResponse::class.java
                    )
                } returns ResponseEntity.ok(userResponse)

                every { userRepository.findFirstByEmail(email) } returns null
                every { passwordUtil.encode(any()) } returns "encoded-password"
                every { userRepository.save(any()) } answers {
                    firstArg<User>().apply { id = 1L }
                }
                every { jwtUtil.generateToken(any()) } returns jwt

                val result = service.login(code, state)

                result.accessToken shouldBe jwt
            }
        }

        When("유저가 이미 존재하면") {
            Then("회원가입 없이 JWT만 발급한다") {
                val existingUser = User(
                    username = name, email = email, passwordHash = "social:naver:$naverId"
                ).apply { id = 999L }

                every {
                    restTemplate.postForEntity(
                        any<String>(), any<HttpEntity<*>>(), eq(NaverTokenResponse::class.java)
                    )
                } returns ResponseEntity.ok(tokenResponse)

                every {
                    restTemplate.exchange(
                        any<String>(), any<HttpMethod>(), any<HttpEntity<*>>(), eq(NaverUserResponse::class.java)
                    )
                } returns ResponseEntity.ok(userResponse)

                every { userRepository.findFirstByEmail(email) } returns existingUser
                every { jwtUtil.generateToken(any()) } returns jwt

                val result = service.login(code, state)

                result.accessToken shouldBe jwt
            }
        }
    }
})
