package com.lucas.aladin.controllers

import com.lucas.aladin.services.TodoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.lucas.aladin.dtos.todo.TodoCreateRequest
import com.lucas.aladin.dtos.todo.TodoResponse
import com.lucas.aladin.dtos.todo.TodoUpdateRequest
import com.lucas.aladin.utils.AuthenticationUtil

@RestController
@RequestMapping("/todos")
class TodoController(
    private val todoService: TodoService
) {

    @PostMapping
    fun createTodo(@RequestBody todoRequest: TodoCreateRequest): ResponseEntity<TodoResponse> {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        return ResponseEntity.ok(todoService.createTodo(todoRequest, authUser.id))
    }

    @GetMapping
    fun getTodos(): ResponseEntity<List<TodoResponse>> {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        return ResponseEntity.ok(todoService.getTodos(authUser.id))
    }

    @GetMapping("/{id}")
    fun getTodo(@PathVariable id: Long): ResponseEntity<TodoResponse> {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        return ResponseEntity.ok(todoService.getTodo(id, authUser.id))
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody todoRequest: TodoUpdateRequest
    ): ResponseEntity<TodoResponse> {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        return ResponseEntity.ok(todoService.updateTodo(id, todoRequest, authUser.id))
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: Long): ResponseEntity<Void> {
        val authUser = AuthenticationUtil.getAuthenticatedUser()
        todoService.deleteTodo(id, authUser.id)
        return ResponseEntity.noContent().build()
    }

}