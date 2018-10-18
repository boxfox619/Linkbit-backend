package com.boxfox.cross.service.auth

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.util.AddressUtil
import com.google.api.client.http.HttpStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.linkbit.android.entity.UserModel
import io.one.sys.db.Tables.ACCOUNT
import org.jooq.DSLContext
import java.util.concurrent.ExecutionException

class AuthServiceImpl : AuthUsecase{

    @Throws(RoutingException::class)
    override fun signin(ctx: DSLContext, token: String): UserModel {
        var decodedToken: FirebaseToken
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token).get()
            if (decodedToken != null) {
                val user = UserModel()
                user.uid = decodedToken.uid
                user.name = decodedToken.name
                user.email = decodedToken.email
                user.profileUrl = decodedToken.picture
                val result = ctx.selectFrom(ACCOUNT)
                        .where(ACCOUNT.UID.eq(user.uid)).fetch()
                if (result.size == 0) {
                    val address = AddressUtil.createRandomAddress(ctx)
                    user.linkbitAddress = address
                    ctx.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME,
                            ACCOUNT.PROFILE, ACCOUNT.ADDRESS)
                            .values(decodedToken.uid, decodedToken.email,
                                    decodedToken.name, user.profileUrl, address).execute()
                    /*FacebookAuth.getFriends(accessToken).setHandler(event -> {
                            if (event.succeeded()) {
                                event.result().forEach(p -> create.insertInto(FRIEND).values(profile.getUid(), p.getUid()).execute());
                            }
                        });*/
                } else {
                    user.linkbitAddress = result[0].address
                }
                return user
            } else {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_BAD_REQUEST, "Not a vaild token")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
        } catch (e: ExecutionException) {
            e.printStackTrace()
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
        }

    }

    @Throws(RoutingException::class)
    override fun getAccountByUid(ctx: DSLContext, uid: String): UserModel {
        val account = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().stream().findFirst().orElse(null)
        if (account == null) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "User not found")
        } else {
            val user = UserModel()
            user.uid = account.uid
            user.email = account.email
            user.linkbitAddress = account.address
            user.name = account.name
            user.profileUrl = account.profile
            return user
        }
    }

    override fun unRegister(ctx: DSLContext, uid: String) {
        val result = ctx.deleteFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).execute()
        if (result == 0) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, "account delete fail")
        }
    }
}
