package com.lucas.aladin.repositories

import com.lucas.aladin.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {

    fun findFirstByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean

}