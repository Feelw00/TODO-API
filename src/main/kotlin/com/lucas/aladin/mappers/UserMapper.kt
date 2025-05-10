package com.lucas.aladin.mappers

import com.lucas.aladin.dtos.user.UserResponse
import com.lucas.aladin.dtos.user.UserSignupRequest
import com.lucas.aladin.entities.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface UserMapper {

    fun userToResponse(user: User): UserResponse

    @Mapping(source = "password", target = "passwordHash")
    fun signupRequestToEntity(request: UserSignupRequest): User

}