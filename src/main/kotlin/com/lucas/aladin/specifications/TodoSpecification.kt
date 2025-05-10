package com.lucas.aladin.specifications

import com.lucas.aladin.dtos.todo.TodoSearchRequest
import com.lucas.aladin.entities.Todo
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.Predicate

object TodoSpecification {
    fun from(request: TodoSearchRequest): Specification<Todo> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            request.title?.let {
                predicates.add(cb.like(cb.lower(root.get("title")), "%${it.lowercase()}%"))
            }

            request.completed?.let {
                predicates.add(cb.equal(root.get<Boolean>("completed"), it))
            }

            request.dueDateFrom?.let {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), it))
            }

            request.dueDateTo?.let {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), it))
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}