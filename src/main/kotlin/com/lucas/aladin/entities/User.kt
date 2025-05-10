package com.lucas.aladin.entities

import com.lucas.aladin.entities.common.BaseSoftDeletableEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
class User(

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

) : BaseSoftDeletableEntity()