package com.boxfox.cross.service.auth

import com.linkbit.android.entity.UserModel
import io.reactivex.Completable
import io.reactivex.Single
import org.jooq.DSLContext

interface AuthUsecase {
    fun signin(ctx: DSLContext, token: String): UserModel
    fun getAccountByUid(ctx: DSLContext, uid: String): UserModel
    fun unRegister(ctx: DSLContext, uid: String)
}
