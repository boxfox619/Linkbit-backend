package com.boxfox.cross.service.friend

import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.UserModel
import io.reactivex.Completable
import io.reactivex.Single


class FriendService(private val impl: FriendServiceImpl = FriendServiceImpl()) : JooqReactiveService(), FriendUsecase {

    override fun loadFriends(uid: String): Single<List<UserModel>> {
        return createSingle { ctx -> impl.loadFriends(ctx, uid) }
    }

    override fun getUser(uid: String): Single<UserModel> {
        return createSingle { ctx -> impl.getUser(ctx, uid) }
    }

    override fun addFriend(ownUid: String, uid: String): Completable {
        return createCompletable { ctx -> impl.addFriend(ctx, ownUid, uid) }
    }

    override fun deleteFriend(ownUid: String, uid: String): Completable {
        return createCompletable { ctx -> impl.deleteFriend(ctx, ownUid, uid) }
    }

    override fun serachUsers(text: String): Single<List<UserModel>> {
        return createSingle { ctx -> impl.searchUser(ctx, text) }
    }
}
