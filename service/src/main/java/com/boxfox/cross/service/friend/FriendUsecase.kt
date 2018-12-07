package com.boxfox.cross.service.friend

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.entity.UserModel
import org.jooq.DSLContext

interface FriendUsecase {

    @Throws(RoutingException::class)
    fun loadFriends(ctx: DSLContext, uid: String): List<UserModel>

    @Throws(RoutingException::class)
    fun getUser(ctx: DSLContext, uid: String): UserModel

    @Throws(RoutingException::class)
    fun addFriend(ctx: DSLContext, ownUid: String, uid: String)

    @Throws(RoutingException::class)
    fun deleteFriend(ctx: DSLContext, ownUid: String, uid: String)

    @Throws(RoutingException::class)
    fun searchUser(ctx: DSLContext, text: String): List<UserModel>
}
