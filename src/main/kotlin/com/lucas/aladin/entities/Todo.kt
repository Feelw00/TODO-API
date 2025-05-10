package com.lucas.aladin.entities

import com.lucas.aladin.entities.common.BaseSoftDeletableEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import java.time.OffsetDateTime

@Entity
@Table(
    name = "todos",
    indexes = [
        Index(name = "idx_todos_title", columnList = "title"),
        Index(name = "idx_todos_due_date", columnList = "due_date"),
        Index(name = "idx_todos_completed", columnList = "completed"),
    ]
)
@SQLDelete(sql = "UPDATE todos SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
class Todo(

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "due_date")
    var dueDate: OffsetDateTime? = null,

    @Column(name = "completed", nullable = false)
    var completed: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    var owner: User? = null,

) : BaseSoftDeletableEntity()