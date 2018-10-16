package com.boxfox.cross.service.auth;

import com.linkbit.android.entity.UserModel;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface AuthUsecase {
    Single<UserModel> signin(String token);
    Single<UserModel> getAccountByUid(String uid);
    Completable unRegister(String uid);
}
