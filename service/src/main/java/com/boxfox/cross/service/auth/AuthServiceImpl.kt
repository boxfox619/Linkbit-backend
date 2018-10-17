package com.boxfox.cross.service.auth

import io.one.sys.db.Tables.ACCOUNT

import com.boxfox.cross.service.ServiceException
import com.boxfox.cross.util.AddressUtil
import com.google.api.client.http.HttpStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.linkbit.android.entity.UserModel
import java.util.concurrent.ExecutionException
import org.jooq.DSLContext

class AuthServiceImpl {

    @Throws(ServiceException::class)
    fun signin(ctx: DSLContext, token: String): UserModel {
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
                throw ServiceException(HttpStatusCodes.STATUS_CODE_BAD_REQUEST, "Not a vaild token")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            throw ServiceException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
        } catch (e: ExecutionException) {
            e.printStackTrace()
            throw ServiceException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
        }

    }

    @Throws(ServiceException::class)
    fun getAccountByUid(ctx: DSLContext, uid: String): UserModel {
        val account = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().stream().findFirst().orElse(null)
        if (account == null) {
            throw ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "User not found")
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

    fun unRegister(ctx: DSLContext, uid: String): Boolean {
        val result = ctx.deleteFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).execute()
        return result > 0
    }
}
