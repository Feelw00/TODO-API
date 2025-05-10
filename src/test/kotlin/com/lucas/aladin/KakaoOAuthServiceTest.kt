package com.lucas.aladin

import com.lucas.aladin.dtos.auth.KakaoAccount
import com.lucas.aladin.dtos.auth.KakaoProfile
import com.lucas.aladin.dtos.auth.KakaoTokenResponse
import com.lucas.aladin.dtos.auth.KakaoUserResponse
import com.lucas.aladin.entities.User
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.services.KakaoOAuthService
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
class KakaoOAuthServiceTest : BehaviorSpec({

    val restTemplate = mockk<RestTemplate>()
    val userRepository = mockk<UserRepository>()
    val jwtUtil = mockk<JwtUtil>()
    val passwordUtil = mockk<PasswordUtil>()

    val clientId = "test-client-id"
    val redirectUri = "http://localhost/auth/kakao/callback"

    val service = KakaoOAuthService(
        clientId = clientId,
        redirectUri = redirectUri,
        userRepository = userRepository,
        jwtUtil = jwtUtil,
        passwordUtil = passwordUtil
    ).apply {
        val field = KakaoOAuthService::class.java.getDeclaredField("restTemplate")
        field.isAccessible = true
        field.set(this, restTemplate)
    }

    given("Kakao 소셜 로그인 요청이 들어왔을 때") {

        val code = "mock-code"
        val kakaoAccessToken = "mock-kakao-access-token"
        val kakaoUserId = 12345L
        val kakaoEmail = "kakao@test.com"
        val kakaoNickname = "테스트사용자"

        val tokenResponse = KakaoTokenResponse(
            accessToken = kakaoAccessToken, refreshToken = "mock-refresh-token"
        )

        val userResponse = KakaoUserResponse(
            id = kakaoUserId, kakaoAccount = KakaoAccount(
                email = kakaoEmail, profile = KakaoProfile(nickname = kakaoNickname)
            )
        )

        val jwt = "jwt-token"

        beforeTest {
            clearAllMocks()
        }

        When("해당 이메일로 유저가 존재하지 않는 경우") {
            Then("회원가입 후 JWT를 발급한다") {
                every {
                    restTemplate.postForEntity(
                        "https://kauth.kakao.com/oauth/token", any(), KakaoTokenResponse::class.java
                    )
                } returns ResponseEntity.ok(tokenResponse)

                every {
                    restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, any(), KakaoUserResponse::class.java
                    )
                } returns ResponseEntity.ok(userResponse)

                every { userRepository.findFirstByEmail(kakaoEmail) } returns null
                every { passwordUtil.encode(any()) } returns "encoded-password"
                every { userRepository.save(any()) } answers {
                    val user = firstArg<User>()
                    user.apply { id = 123L }
                }
                every { jwtUtil.generateToken(any()) } returns jwt

                val result = service.login(code)

                result.accessToken shouldBe jwt
            }
        }

        When("해당 이메일로 유저가 이미 존재하는 경우") {
            Then("회원가입 없이 JWT만 발급한다") {
                val existingUser = User(
                    username = kakaoNickname, email = kakaoEmail, passwordHash = "social:kakao:$kakaoUserId"
                ).apply { id = 1L }

                every {
                    restTemplate.postForEntity(
                        any<String>(), any<HttpEntity<*>>(), eq(KakaoTokenResponse::class.java)
                    )
                } returns ResponseEntity.ok(tokenResponse)

                every {
                    restTemplate.exchange(
                        any<String>(), any<HttpMethod>(), any<HttpEntity<*>>(), eq(KakaoUserResponse::class.java)
                    )
                } returns ResponseEntity.ok(userResponse)

                every { userRepository.findFirstByEmail(kakaoEmail) } returns existingUser
                every { jwtUtil.generateToken(any()) } returns jwt

                val result = service.login(code)

                result.accessToken shouldBe jwt
            }
        }
    }
})
