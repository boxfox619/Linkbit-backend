package com.boxfox.cross.service.auth

import com.linkbit.android.entity.UserModel
import io.reactivex.Completable
import io.reactivex.Single

interface AuthUsecase {
    fun signin(token: String): Single<UserModel>
    fun getAccountByUid(uid: String): Single<UserModel>
    fun unRegister(uid: String): Completable
}
