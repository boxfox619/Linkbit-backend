package com.boxfox.cross.service.auth

import com.boxfox.cross.common.entity.UserModel
import com.boxfox.cross.service.JooqReactiveService
import io.reactivex.Completable
import io.reactivex.Single

class AuthService(private val impl: AuthServiceImpl = AuthServiceImpl()) : JooqReactiveService() {

    fun signin(token: String): Single<UserModel> = single { ctx -> impl.signin(ctx, token) }

    fun getAccountByUid(uid: String): Single<UserModel> = single { ctx -> impl.getAccountByUid(ctx, uid) }

    fun unRegister(uid: String): Completable = completable { ctx -> impl.unRegister(ctx, uid) }
}
