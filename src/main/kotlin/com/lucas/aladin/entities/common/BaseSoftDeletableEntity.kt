package com.lucas.aladin.entities.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef
import java.time.OffsetDateTime

@MappedSuperclass
@FilterDef(
    name = "deletedFilter",
)
@Filter(
    name = "deletedFilter", condition = "deleted_at IS NULL"
)
abstract class BaseSoftDeletableEntity : BaseEntity() {
    @Column(name = "deleted_at")
    var deletedAt: OffsetDateTime? = null
}