package com.boxfox.cross.service.auth

import com.boxfox.cross.common.entity.UserModel
import org.jooq.DSLContext

interface AuthUsecase {
    fun signin(ctx: DSLContext, token: String): UserModel
    fun getAccountByUid(ctx: DSLContext, uid: String): UserModel
    fun unRegister(ctx: DSLContext, uid: String)
}
