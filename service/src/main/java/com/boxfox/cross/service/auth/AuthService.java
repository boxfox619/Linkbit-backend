package com.boxfox.cross.service.auth;

import com.boxfox.cross.service.JooqReactiveService;
import com.linkbit.android.entity.UserModel;
import io.reactivex.Completable;
import io.reactivex.Single;


public class AuthService extends JooqReactiveService implements AuthUsecase {
    private final AuthServiceImpl impl;

    public AuthService() {
        impl = new AuthServiceImpl();
    }

    @Override
    public Single<UserModel> signin(String token) {
        return createSingle((ctx) -> impl.signin(ctx, token));
    }

    @Override
    public Single<UserModel> getAccountByUid(String uid) {
        return createSingle(ctx -> impl.getAccountByUid(ctx, uid));
    }

    @Override
    public Completable unRegister(String uid) {
        return createCompletable(ctx -> impl.unRegister(ctx, uid));
    }
}
