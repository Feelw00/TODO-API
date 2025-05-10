package com.lucas.aladin.services

import com.lucas.aladin.configs.exceptions.ForbiddenTodoAccessException
import com.lucas.aladin.configs.exceptions.NoUpdatableFieldException
import com.lucas.aladin.configs.exceptions.TodoNotFoundException
import com.lucas.aladin.configs.exceptions.UserNotFoundException
import com.lucas.aladin.dtos.todo.TodoCreateRequest
import com.lucas.aladin.dtos.todo.TodoResponse
import com.lucas.aladin.dtos.todo.TodoSearchRequest
import com.lucas.aladin.dtos.todo.TodoUpdateRequest
import com.lucas.aladin.entities.Todo
import com.lucas.aladin.entities.User
import com.lucas.aladin.mappers.TodoMapper
import com.lucas.aladin.repositories.TodoRepository
import com.lucas.aladin.repositories.UserRepository
import com.lucas.aladin.specifications.TodoSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val userRepository: UserRepository,
    private val todoMapper: TodoMapper,
) {

    fun createTodo(request: TodoCreateRequest, userId: Long): TodoResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val todo = todoMapper.todoCreateRequestToEntity(request).apply { owner = user }
        return todoMapper.todoToResponse(todoRepository.save(todo))
    }

    fun getTodos(userId: Long): List<TodoResponse> {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        return todoMapper.todoToResponse(todoRepository.findAllByOwner(user))
    }

    fun getTodo(id: Long, userId: Long): TodoResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val todo = todoRepository.findById(id).orElseThrow { TodoNotFoundException() }
        if (todo.owner?.id != user.id) throw ForbiddenTodoAccessException(todo.id!!)
        return todoMapper.todoToResponse(todo)
    }

    fun searchTodos(
        request: TodoSearchRequest, pageable: Pageable, userId: Long
    ): Page<TodoResponse> {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val spec = TodoSpecification.from(request).and(ownerIs(user))
        return todoRepository.findAll(spec, pageable).map { todoMapper.todoToResponse(it) }
    }

    fun updateTodo(id: Long, request: TodoUpdateRequest, userId: Long): TodoResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val todo = todoRepository.findById(id).orElseThrow { TodoNotFoundException() }
        if (todo.owner?.id != user.id) throw ForbiddenTodoAccessException(todo.id!!)
        if (request.title == null && request.description == null && request.dueDate == null && request.completed == null) {
            throw NoUpdatableFieldException()
        }

        request.title?.let { todo.title = it }
        request.description?.let { todo.description = it }
        request.dueDate?.let { todo.dueDate = it }
        request.completed?.let { todo.completed = it }

        return todoMapper.todoToResponse(todoRepository.save(todo))
    }

    fun deleteTodo(id: Long, userId: Long) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val todo = todoRepository.findById(id).orElseThrow { TodoNotFoundException() }
        if (todo.owner?.id != user.id) throw ForbiddenTodoAccessException(todo.id!!)
        todoRepository.delete(todo)
    }

    private fun ownerIs(user: User): Specification<Todo> {
        return Specification { root, _, cb ->
            cb.equal(root.get<User>("owner"), user)
        }
    }
}