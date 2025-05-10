package com.lucas.aladin.mappers

import com.lucas.aladin.dtos.todo.TodoCreateRequest
import com.lucas.aladin.dtos.todo.TodoResponse
import com.lucas.aladin.entities.Todo
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface TodoMapper {

    fun todoToResponse(todo: Todo): TodoResponse

    fun todoToResponse(todos: List<Todo>): List<TodoResponse>

    fun todoCreateRequestToEntity(request: TodoCreateRequest): Todo

}