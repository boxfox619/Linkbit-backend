package com.boxfox.linkbit.service.friend

import com.boxfox.linkbit.common.entity.UserModel
import io.reactivex.Completable
import io.reactivex.Single


class FriendService(private val impl: com.boxfox.linkbit.service.friend.FriendServiceImpl = com.boxfox.linkbit.service.friend.FriendServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {

    fun loadFriends(uid: String): Single<List<UserModel>> {
        return single { ctx -> impl.loadFriends(ctx, uid) }
    }

    fun getUser(uid: String): Single<UserModel> {
        return single { ctx -> impl.getUser(ctx, uid) }
    }

    fun addFriend(ownUid: String, uid: String): Completable {
        return completable { ctx -> impl.addFriend(ctx, ownUid, uid) }
    }

    fun deleteFriend(ownUid: String, uid: String): Completable {
        return completable { ctx -> impl.deleteFriend(ctx, ownUid, uid) }
    }

    fun serachUsers(text: String): Single<List<UserModel>> {
        return single { ctx -> impl.searchUser(ctx, text) }
    }
}
