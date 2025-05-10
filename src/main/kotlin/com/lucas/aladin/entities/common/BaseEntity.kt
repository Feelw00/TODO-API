package com.lucas.aladin.entities.common

import jakarta.persistence.*
import jakarta.persistence.MappedSuperclass
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}