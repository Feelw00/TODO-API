package com.lucas.aladin.configs

import jakarta.persistence.EntityManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.hibernate.Session
import org.springframework.stereotype.Component

@Aspect
@Component
class SoftDeleteFilterAspect(
    private val entityManager: EntityManager
) {

    @Around("execution(* com.lucas.aladin.services.*.*(..))")
    fun applySoftDeleteFilter(joinPoint: ProceedingJoinPoint): Any? {
        val session = entityManager.unwrap(Session::class.java)
        session.enableFilter("deletedFilter")
        return try {
            joinPoint.proceed()
        } finally {
            session.disableFilter("deletedFilter")
        }
    }
}