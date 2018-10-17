package com.boxfox.cross.service.friend

import com.linkbit.android.entity.UserModel
import io.reactivex.Completable
import io.reactivex.Single

interface FriendUsecase {

    fun loadFriends(uid: String): Single<List<UserModel>>

    fun getUser(uid: String): Single<UserModel>

    fun addFriend(ownUid: String, uid: String): Completable

    fun deleteFriend(ownUid: String, uid: String): Completable

    fun serachUsers(text: String): Single<List<UserModel>>
}
