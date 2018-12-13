package com.boxfox.cross.common.entity

import org.jooq.Record

interface EntityMapper<T, V>{
    fun toEntity(v: V):T
    fun toEntity(record: Record): T
}