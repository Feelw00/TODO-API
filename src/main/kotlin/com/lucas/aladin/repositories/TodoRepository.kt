package com.lucas.aladin.repositories

import com.lucas.aladin.entities.Todo
import com.lucas.aladin.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TodoRepository: JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {

    fun findAllByOwner(owner: User): List<Todo>
}