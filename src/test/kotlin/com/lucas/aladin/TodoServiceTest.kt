package com.lucas.aladin

import com.lucas.aladin.configs.exceptions.ForbiddenTodoAccessException
import com.lucas.aladin.configs.exceptions.NoUpdatableFieldException
import com.lucas.aladin.configs.exceptions.TodoNotFoundException
import com.lucas.aladin.configs.exceptions.UserNotFoundException
import com.lucas.aladin.dtos.todo.TodoCreateRequest
import com.lucas.aladin.dtos.todo.TodoSearchRequest
import com.lucas.aladin.dtos.todo.TodoUpdateRequest
import com.lucas.aladin.entities.Todo
import com.lucas.aladin.entities.User
import com.lucas.aladin.repositories.TodoRepository
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.services.TodoService
import com.lucas.aladin.utils.PasswordUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import java.time.OffsetDateTime

@SpringBootTest
@ActiveProfiles("test")
class TodoServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val todoRepository: TodoRepository,
    private val todoService: TodoService,
    private val passwordUtil: PasswordUtil,
) : BehaviorSpec({

    lateinit var user: User

    beforeTest {
        userRepository.deleteAll()
        todoRepository.deleteAll()

        user = userRepository.save(
            User(
                username = "testuser", email = "todo@example.com", passwordHash = passwordUtil.encode("pass123")
            )
        )
    }

    context("TodoService Test") {
        Given("createTodo") {
            When("정상적인 요청을 하면") {
                Then("Todo가 저장되고 응답된다") {

                    val request = TodoCreateRequest(
                        title = "할 일 작성", description = "테스트용 할 일입니다.", dueDate = OffsetDateTime.now().plusDays(3)
                    )

                    val result = todoService.createTodo(request, user.id!!)

                    result.title shouldBe "할 일 작성"
                    result.description shouldBe "테스트용 할 일입니다."
                    result.completed shouldBe false
                }
            }

            When("존재하지 않는 유저 ID로 요청하면") {
                Then("UserNotFoundException이 발생한다") {
                    val invalidUserId = -1L

                    val request = TodoCreateRequest(
                        title = "무효 사용자", description = "이 요청은 실패해야 합니다."
                    )

                    shouldThrow<UserNotFoundException> {
                        todoService.createTodo(request, invalidUserId)
                    }
                }
            }
        }

        Given("getTodos") {
            When("해당 유저가 작성한 Todo가 여러 개 있을 때") {
                Then("모두 조회된다") {
                    todoRepository.saveAll(
                        listOf(
                            Todo(title = "할 일 1", owner = user), Todo(title = "할 일 2", owner = user)
                        )
                    )

                    val result = todoService.getTodos(user.id!!)

                    result.size shouldBe 2
                    result.map { it.title } shouldContainExactlyInAnyOrder listOf("할 일 1", "할 일 2")
                }
            }

            When("존재하지 않는 유저 ID로 조회하면") {
                Then("UserNotFoundException이 발생한다") {
                    shouldThrow<UserNotFoundException> {
                        todoService.getTodos(-1L)
                    }
                }
            }
        }

        Given("getTodo") {
            When("본인의 Todo ID로 조회하면") {
                Then("TodoResponse가 반환된다") {
                    val todo = todoRepository.save(Todo(title = "내 할 일", owner = user))

                    val result = todoService.getTodo(todo.id!!, user.id!!)

                    result.id shouldBe todo.id
                    result.title shouldBe "내 할 일"
                }
            }

            When("존재하지 않는 유저 ID로 조회하면") {
                Then("UserNotFoundException이 발생한다") {
                    val todo = todoRepository.save(
                        Todo(title = "할 일", owner = user)
                    )

                    shouldThrow<UserNotFoundException> {
                        todoService.getTodo(todo.id!!, -1L)
                    }
                }
            }

            When("존재하지 않는 Todo ID로 조회하면") {
                Then("TodoNotFoundException이 발생한다") {
                    shouldThrow<TodoNotFoundException> {
                        todoService.getTodo(-1L, user.id!!)
                    }
                }
            }

            When("다른 사람의 Todo를 조회하면") {
                Then("ForbiddenTodoAccessException이 발생한다") {
                    val other = userRepository.save(
                        User(
                            username = "other", email = "other@example.com", passwordHash = passwordUtil.encode("other")
                        )
                    )
                    val todo = todoRepository.save(Todo(title = "남의 할 일", owner = other))

                    shouldThrow<ForbiddenTodoAccessException> {
                        todoService.getTodo(todo.id!!, user.id!!)
                    }
                }
            }
        }

        Given("searchTodos") {
            todoRepository.deleteAll()

            When("제목 키워드로 검색하면") {
                Then("해당 키워드를 포함한 Todo만 반환된다") {
                    todoRepository.saveAll(
                        listOf(
                            Todo(title = "청소하기", owner = user),
                            Todo(title = "설거지", owner = user),
                            Todo(title = "청소장 보기", owner = user)
                        )
                    )

                    val request = TodoSearchRequest(title = "청소")
                    val result = todoService.searchTodos(request, Pageable.unpaged(), user.id!!)

                    result.content.size shouldBe 2
                    result.content.map { it.title } shouldContainExactlyInAnyOrder listOf("청소하기", "청소장 보기")
                }
            }

            When("완료 여부로 필터링하면") {
                Then("completed 상태에 맞는 Todo만 조회된다") {
                    todoRepository.saveAll(
                        listOf(
                            Todo(title = "완료됨", completed = true, owner = user),
                            Todo(title = "미완료", completed = false, owner = user)
                        )
                    )

                    val request = TodoSearchRequest(completed = true)
                    val result = todoService.searchTodos(request, Pageable.unpaged(), user.id!!)

                    result.content.size shouldBe 1
                    result.content.first().title shouldBe "완료됨"
                }
            }

            When("마감일 범위로 검색하면") {
                Then("지정한 날짜 안에 있는 Todo만 조회된다") {
                    val now = OffsetDateTime.now()
                    todoRepository.saveAll(
                        listOf(
                            Todo(title = "내일 마감", dueDate = now.plusDays(1), owner = user),
                            Todo(title = "다음주 마감", dueDate = now.plusDays(7), owner = user),
                            Todo(title = "마감 없음", dueDate = null, owner = user)
                        )
                    )

                    val request = TodoSearchRequest(
                        dueDateFrom = now,
                        dueDateTo = now.plusDays(3)
                    )

                    val result = todoService.searchTodos(request, Pageable.unpaged(), user.id!!)
                    result.content.size shouldBe 1
                    result.content.first().title shouldBe "내일 마감"
                }
            }

            When("다른 유저의 Todo는 조회되지 않는다") {
                Then("결과는 비어 있다") {
                    val other = userRepository.save(
                        User(
                            username = "other",
                            email = "other@example.com",
                            passwordHash = passwordUtil.encode("other")
                        )
                    )
                    todoRepository.save(Todo(title = "남의 할일", owner = other))

                    val request = TodoSearchRequest()
                    val result = todoService.searchTodos(request, Pageable.unpaged(), user.id!!)

                    result.content shouldBe emptyList()
                }
            }

            When("페이지 요청을 하면") {
                Then("지정한 수만큼 결과가 잘린다") {
                    repeat(10) {
                        todoRepository.save(Todo(title = "할일$it", owner = user))
                    }

                    val request = TodoSearchRequest()
                    val result = todoService.searchTodos(request, PageRequest.of(0, 5), user.id!!)

                    result.content.size shouldBe 5
                    result.totalElements shouldBe 10
                }
            }
        }

        Given("updateTodo") {
            When("정상적인 요청을 하면") {
                Then("Todo가 수정된다") {
                    val todo = todoRepository.save(
                        Todo(title = "원래 제목", description = "원래 내용", completed = false, owner = user)
                    )

                    val request = TodoUpdateRequest(
                        title = "수정된 제목", description = "수정된 설명", completed = true
                    )

                    val result = todoService.updateTodo(todo.id!!, request, user.id!!)

                    result.title shouldBe "수정된 제목"
                    result.description shouldBe "수정된 설명"
                    result.completed shouldBe true
                }
            }

            When("존재하지 않는 유저 ID로 요청하면") {
                Then("UserNotFoundException이 발생한다") {
                    val todo = todoRepository.save(Todo(title = "할일", owner = user))

                    val request = TodoUpdateRequest(title = "업데이트")

                    shouldThrow<UserNotFoundException> {
                        todoService.updateTodo(todo.id!!, request, -1L)
                    }
                }
            }

            When("존재하지 않는 Todo ID로 요청하면") {
                Then("TodoNotFoundException이 발생한다") {
                    val request = TodoUpdateRequest(title = "업데이트")

                    shouldThrow<TodoNotFoundException> {
                        todoService.updateTodo(-1L, request, user.id!!)
                    }
                }
            }

            When("다른 유저의 Todo를 수정하려 하면") {
                Then("ForbiddenTodoAccessException이 발생한다") {
                    val other = userRepository.save(
                        User(
                            username = "other",
                            email = "other@example.com",
                            passwordHash = passwordUtil.encode("otherpass")
                        )
                    )

                    val todo = todoRepository.save(Todo(title = "남의 할 일", owner = other))
                    val request = TodoUpdateRequest(title = "수정 시도")

                    shouldThrow<ForbiddenTodoAccessException> {
                        todoService.updateTodo(todo.id!!, request, user.id!!)
                    }
                }
            }

            When("모든 필드가 null이면") {
                Then("NoUpdatableFieldException이 발생한다") {
                    val todo = todoRepository.save(Todo(title = "업데이트 대상", owner = user))
                    val request = TodoUpdateRequest() // 모든 필드 null

                    shouldThrow<NoUpdatableFieldException> {
                        todoService.updateTodo(todo.id!!, request, user.id!!)
                    }
                }
            }
        }

        Given("deleteTodo") {
            When("본인의 Todo를 삭제 요청하면") {
                Then("soft delete 되어 deletedAt이 설정된다") {
                    val todo = todoRepository.save(
                        Todo(title = "삭제할 할일", owner = user)
                    )

                    todoService.deleteTodo(todo.id!!, user.id!!)

                    val deleted = todoRepository.findById(todo.id!!).get()
                    deleted.deletedAt shouldNotBe null
                }
            }

            When("존재하지 않는 유저 ID로 요청하면") {
                Then("UserNotFoundException이 발생한다") {
                    val todo = todoRepository.save(Todo(title = "할일", owner = user))

                    shouldThrow<UserNotFoundException> {
                        todoService.deleteTodo(todo.id!!, -1L)
                    }
                }
            }

            When("존재하지 않는 Todo ID로 요청하면") {
                Then("TodoNotFoundException이 발생한다") {
                    shouldThrow<TodoNotFoundException> {
                        todoService.deleteTodo(-1L, user.id!!)
                    }
                }
            }

            When("다른 사람의 Todo를 삭제하려고 하면") {
                Then("ForbiddenTodoAccessException이 발생한다") {
                    val other = userRepository.save(
                        User(
                            username = "other",
                            email = "other@example.com",
                            passwordHash = passwordUtil.encode("otherpass")
                        )
                    )

                    val todo = todoRepository.save(Todo(title = "남의 할일", owner = other))

                    shouldThrow<ForbiddenTodoAccessException> {
                        todoService.deleteTodo(todo.id!!, user.id!!)
                    }
                }
            }
        }
    }
})