package com.boxfox.linkbit.service.auth

import com.boxfox.linkbit.common.entity.UserModel
import org.jooq.DSLContext

interface AuthUsecase {
    fun signin(ctx: DSLContext, token: String): UserModel
    fun getAccountByUid(ctx: DSLContext, uid: String): UserModel
    fun unRegister(ctx: DSLContext, uid: String)
}
