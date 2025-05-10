package com.lucas.aladin.configs.exceptions

class EmailAlreadyExistsException(email: String) : RuntimeException("이미 사용 중인 이메일입니다: $email")

class EmailNotFoundException(email: String) : RuntimeException("해당 이메일을 가진 사용자가 존재하지 않습니다: $email")

class InvalidPasswordException(message: String? = null) : RuntimeException(message ?: "비밀번호가 일치하지 않습니다.")

class UserNotFoundException : RuntimeException("존재하지 않는 사용자입니다.")

class TodoNotFoundException : RuntimeException("존재하지 않는 Todo입니다.")

class NoUpdatableFieldException : RuntimeException("변경할 항목이 없습니다.")

class ForbiddenTodoAccessException(todoId: Long) : RuntimeException("해당 Todo에 접근할 수 없습니다: id=$todoId")
