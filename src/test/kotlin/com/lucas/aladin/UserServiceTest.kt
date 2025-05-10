package com.lucas.aladin

import com.lucas.aladin.configs.exceptions.*
import com.lucas.aladin.dtos.user.UserLoginRequest
import com.lucas.aladin.dtos.user.UserSignupRequest
import com.lucas.aladin.dtos.user.UserUpdateRequest
import com.lucas.aladin.entities.User
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.services.UserService
import com.lucas.aladin.utils.JwtUtil
import com.lucas.aladin.utils.PasswordUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val passwordUtil: PasswordUtil,
    private val jwtUtil: JwtUtil,
) : BehaviorSpec({

    beforeTest {
        userRepository.deleteAll()
    }

    afterTest {
        userRepository.deleteAll()
    }

    context("UserService Test") {
        Given("signup") {
             When("정상적인 회원가입 요청을 하면") {
                 Then("유저가 저장된다") {
                     val request = UserSignupRequest(
                         username = "testuser",
                         email = "test@example.com",
                         password = "secure1234"
                     )

                     val result = userService.signup(request)

                     result.accessToken shouldContain "."
                     userRepository.findFirstByEmail("test@example.com") shouldNotBe null
                 }
             }

            When("이미 존재하는 이메일로 회원가입하면") {
                Then("EmailAlreadyExistsException이 발생한다") {
                    val existing = User(
                        username = "existing-user",
                        email = "test@example.com",
                        passwordHash = passwordUtil.encode("some-password")
                    )
                    userRepository.save(existing)

                    val request = UserSignupRequest(
                        username = "new-user",
                        email = "test@example.com",
                        password = "secure1234"
                    )

                    shouldThrow<EmailAlreadyExistsException> {
                        userService.signup(request)
                    }.message shouldContain "test@example.com"
                }
            }
        }

        Given("login") {
            When("올바른 이메일과 비밀번호로 로그인하면") {
                Then("JWT 토큰이 발급된다") {
                    val rawPassword = "secure1234"
                    val encodedPassword = passwordUtil.encode(rawPassword)
                    val existingUser = User(
                        username = "testuser",
                        email = "test@example.com",
                        passwordHash = encodedPassword
                    )
                    userRepository.save(existingUser)

                    val request = UserLoginRequest(
                        email = "test@example.com",
                        password = rawPassword
                    )

                    val result = userService.login(request)

                    result.accessToken shouldNotBe null
                    result.accessToken shouldContain "."
                }
            }

            When("존재하지 않는 이메일로 로그인하면") {
                Then("EmailNotFoundException이 발생한다") {
                    val request = UserLoginRequest(
                        email = "nonexistent@example.com",
                        password = "somepassword"
                    )

                    val exception = shouldThrow<EmailNotFoundException> {
                        userService.login(request)
                    }

                    exception.message shouldContain "nonexistent@example.com"
                }
            }

            When("비밀번호가 틀리면") {
                Then("InvalidPasswordException이 발생한다") {
                    val savedUser = User(
                        username = "testuser",
                        email = "login@example.com",
                        passwordHash = passwordUtil.encode("correct-password")
                    )
                    userRepository.save(savedUser)

                    val request = UserLoginRequest(
                        email = "login@example.com",
                        password = "wrong-password"
                    )

                    val exception = shouldThrow<InvalidPasswordException> {
                        userService.login(request)
                    }

                    exception.message shouldContain "비밀번호"
                }
            }
        }

        Given("getMe") {
            When("존재하는 ID로 조회하면") {
                Then("유저 정보가 반환된다") {
                    val user = userRepository.save(
                        User(
                            username = "testuser",
                            email = "test@example.com",
                            passwordHash = passwordUtil.encode("pass123")
                        )
                    )

                    val claims = mapOf(
                        "id" to user.id!!,
                        "email" to user.email,
                        "username" to user.username
                    )
                    val accessToken = jwtUtil.generateToken(claims)
                    val extractedClaims = jwtUtil.decodeToken(accessToken)!!
                    val userIdFromToken = (extractedClaims["id"] as Number).toLong()

                    val result = userService.getMe(userIdFromToken)

                    result.username shouldBe "testuser"
                    result.email shouldBe "test@example.com"
                }
            }

            When("존재하지 않는 ID로 조회하면") {
                Then("UserNotFoundException이 발생한다") {
                    val nonExistentId = -1L

                    val exception = shouldThrow<UserNotFoundException> {
                        userService.getMe(nonExistentId)
                    }

                    exception.message shouldContain "존재하지 않는 사용자입니다."
                }
            }
        }

        Given("updateMe") {
            When("username만 변경 요청하면") {
                Then("username이 정상적으로 변경된다") {
                    val user = userRepository.save(
                        User(
                            username = "original-user",
                            email = "test@example.com",
                            passwordHash = passwordUtil.encode("pass123")
                        )
                    )

                    val request = UserUpdateRequest(
                        username = "updated-user"
                    )

                    val result = userService.updateMe(user.id!!, request)

                    result.username shouldBe "updated-user"
                    result.email shouldBe "test@example.com"
                }
            }

            When("현재 비밀번호와 새 비밀번호를 함께 전달하면") {
                Then("비밀번호가 정상적으로 변경된다") {
                    val user = userRepository.save(
                        User(
                            username = "password-user",
                            email = "pass@example.com",
                            passwordHash = passwordUtil.encode("current-pass")
                        )
                    )

                    val request = UserUpdateRequest(
                        currentPassword = "current-pass",
                        newPassword = "new-secure-pass"
                    )

                    userService.updateMe(user.id!!, request)

                    val updated = userRepository.findById(user.id!!).get()
                    passwordUtil.matches("new-secure-pass", updated.passwordHash) shouldBe true
                }
            }

            When("새 비밀번호만 전달하고 현재 비밀번호가 없으면") {
                Then("InvalidPasswordException이 발생한다") {
                    val user = userRepository.save(
                        User(
                            username = "user",
                            email = "test@example.com",
                            passwordHash = passwordUtil.encode("old-pass")
                        )
                    )

                    val request = UserUpdateRequest(
                        currentPassword = null,
                        newPassword = "new-password"
                    )

                    shouldThrow<InvalidPasswordException> {
                        userService.updateMe(user.id!!, request)
                    }.message shouldContain "현재 비밀번호가 필요"
                }
            }

            When("현재 비밀번호가 틀리면") {
                Then("InvalidPasswordException이 발생한다") {
                    val user = userRepository.save(
                        User(
                            username = "user2",
                            email = "test2@example.com",
                            passwordHash = passwordUtil.encode("correct-pass")
                        )
                    )

                    val request = UserUpdateRequest(
                        currentPassword = "wrong-pass",
                        newPassword = "new-password"
                    )

                    shouldThrow<InvalidPasswordException> {
                        userService.updateMe(user.id!!, request)
                    }.message shouldContain "일치하지 않습니다"
                }
            }

            When("username과 newPassword 둘 다 null이면") {
                Then("NoUpdatableFieldException이 발생한다") {
                    val user = userRepository.save(
                        User(
                            username = "user3",
                            email = "test3@example.com",
                            passwordHash = passwordUtil.encode("password")
                        )
                    )

                    val request = UserUpdateRequest(
                        username = null,
                        currentPassword = null,
                        newPassword = null
                    )

                    shouldThrow<NoUpdatableFieldException> {
                        userService.updateMe(user.id!!, request)
                    }.message shouldContain "변경할 항목이 없습니다"
                }
            }
        }

        given("deleteMe") {
            When("존재하는 유저가 삭제 요청을 하면") {
                Then("soft delete 처리되어 조회되지 않는다") {
                    val user = userRepository.save(
                        User(
                            username = "deletable",
                            email = "del@example.com",
                            passwordHash = passwordUtil.encode("pass123")
                        )
                    )

                    userService.deleteMe(user.id!!)
                    val deletedUser = userRepository.findById(user.id!!).get()
                    deletedUser.deletedAt shouldNotBe null
                }
            }

            When("존재하지 않는 유저 ID로 삭제 요청을 하면") {
                Then("UserNotFoundException이 발생한다") {
                    val nonExistentId = -1L

                    shouldThrow<UserNotFoundException> {
                        userService.deleteMe(nonExistentId)
                    }.message shouldContain "존재하지 않는 사용자입니다."
                }
            }
        }
    }
})