package com.boxfox.cross.service.auth

import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.UserModel
import io.reactivex.Completable
import io.reactivex.Single

class AuthService(private val impl: AuthServiceImpl = AuthServiceImpl()) : JooqReactiveService(), AuthUsecase {

    override fun signin(token: String): Single<UserModel> = createSingle { ctx -> impl.signin(ctx, token) }

    override fun getAccountByUid(uid: String): Single<UserModel> = createSingle { ctx -> impl.getAccountByUid(ctx, uid) }

    override fun unRegister(uid: String): Completable = createCompletable { ctx -> impl.unRegister(ctx, uid) }
}
