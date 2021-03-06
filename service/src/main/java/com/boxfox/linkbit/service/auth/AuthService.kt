package com.boxfox.linkbit.service.auth

import com.boxfox.linkbit.common.entity.UserModel
import com.boxfox.linkbit.service.JooqReactiveService
import io.reactivex.Completable
import io.reactivex.Single

class AuthService(private val impl: com.boxfox.linkbit.service.auth.AuthServiceImpl = com.boxfox.linkbit.service.auth.AuthServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {

    fun signin(token: String): Single<UserModel> = single { ctx -> impl.signin(ctx, token) }

    fun getAccountByUid(uid: String): Single<UserModel> = single { ctx -> impl.getAccountByUid(ctx, uid) }

    fun unRegister(uid: String): Completable = completable { ctx -> impl.unRegister(ctx, uid) }
}
