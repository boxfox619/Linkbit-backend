package com.boxfox.cross.service.friend

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.entity.UserModel
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.Tables.*
import org.jooq.DSLContext
import java.util.*

class FriendServiceImpl : FriendUsecase {

    @Throws(RoutingException::class)
    override fun loadFriends(ctx: DSLContext, uid: String): List<UserModel> {
        val friends = ArrayList<UserModel>()
        val result = ctx.selectFrom(FRIEND).where(FRIEND.UID_1.eq(uid).or(FRIEND.UID_2.eq(uid))).fetch()
        for (r in result) {
            val target = if (r.uid_1 == uid) r.uid_2 else r.uid_1
            val acc = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(target)).fetch().stream().findFirst().orElse(null)
            if (acc == null) {
                deleteFriend(ctx, uid, target)
                continue
            }
            val user = UserModel()
            user.name = acc.name
            user.email = acc.email
            user.profileUrl = acc.profile
            user.uid = acc.uid
            user.linkbitAddress = acc.address
            friends.add(user)
        }
        return friends
    }

    @Throws(RoutingException::class)
    override fun getUser(ctx: DSLContext, uid: String): UserModel {
        val acc = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().stream().findFirst().orElse(null)
                ?: throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "User not found")
        val user = UserModel()
        user.name = acc.name
        user.email = acc.email
        user.profileUrl = acc.profile
        user.uid = acc.uid
        user.linkbitAddress = acc.address
        return user
    }

    @Throws(RoutingException::class)
    override fun addFriend(ctx: DSLContext, ownUid: String, uid: String) {
        if (ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().size == 0) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Target user can not found")
        } else {
            val result = ctx.insertInto(FRIEND, FRIEND.UID_1, FRIEND.UID_2).values(ownUid, uid)
                    .execute()
            if (result == 0) {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, "Update fail")
            }
        }
    }

    @Throws(RoutingException::class)
    override fun deleteFriend(ctx: DSLContext, ownUid: String, uid: String) {
        val accountRecord = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(ownUid)).fetch().stream().findFirst().orElse(null)
        if (accountRecord == null) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Target user can not found")
        } else {
            val result = ctx
                    .deleteFrom(FRIEND)
                    .where(FRIEND.UID_1.equal(ownUid).and(FRIEND.UID_2.equal(uid))).execute()
            if (result == 0) {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, "Update fail")
            }
        }
    }

    @Throws(RoutingException::class)
    override fun searchUser(ctx: DSLContext, text: String): List<UserModel> {
        val accounts = ArrayList<UserModel>()
        val records = ctx.selectFrom(ACCOUNT.join(WALLET).on(ACCOUNT.UID.eq(WALLET.UID)))
                .where(
                        ACCOUNT.ADDRESS.like(text)
                                .or(WALLET.ADDRESS.like(text))
                                .or(WALLET.CROSSADDRESS.like(text))
                                .or(ACCOUNT.EMAIL.like(text))
                                .or(ACCOUNT.NAME.like(text))
                ).fetch()
        records.forEach { r ->
            val user = UserModel()
            user.name = r.getValue(ACCOUNT.NAME)
            user.email = r.getValue(ACCOUNT.EMAIL)
            user.linkbitAddress = r.getValue(ACCOUNT.ADDRESS)
            user.uid = r.getValue(ACCOUNT.UID)
            user.profileUrl = r.getValue(ACCOUNT.PROFILE)
            accounts.add(user)
        }
        return accounts
    }
}
