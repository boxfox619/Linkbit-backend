package com.boxfox.cross.service.auth;

import static io.one.sys.db.Tables.ACCOUNT;

import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.service.ServiceException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.linkbit.android.entity.UserModel;
import io.one.sys.db.tables.records.AccountRecord;
import java.util.concurrent.ExecutionException;
import org.jooq.DSLContext;
import org.jooq.Result;

public class AuthServiceImpl {

    public UserModel signin(DSLContext ctx, String token) throws ServiceException {
        FirebaseToken decodedToken = null;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token).get();
            if (decodedToken != null) {
                UserModel user = new UserModel();
                user.setUid(decodedToken.getUid());
                user.setName(decodedToken.getName());
                user.setEmail(decodedToken.getEmail());
                user.setProfileUrl(decodedToken.getPicture());
                Result<AccountRecord> result = ctx.selectFrom(ACCOUNT)
                    .where(ACCOUNT.UID.eq(user.getUid())).fetch();
                if (result.size() == 0) {
                    String address = AddressService.createRandomAddress(ctx);
                    user.setLinkbitAddress(address);
                    ctx.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME,
                        ACCOUNT.PROFILE, ACCOUNT.ADDRESS)
                        .values(decodedToken.getUid(), decodedToken.getEmail(),
                            decodedToken.getName(), user.getProfileUrl(), address).execute();
                        /*FacebookAuth.getFriends(accessToken).setHandler(event -> {
                            if (event.succeeded()) {
                                event.result().forEach(p -> create.insertInto(FRIEND).values(profile.getUid(), p.getUid()).execute());
                            }
                        });*/
                } else {
                    user.setLinkbitAddress(result.get(0).getAddress());
                }
                return user;
            } else {
                throw new ServiceException(HttpStatusCodes.STATUS_CODE_BAD_REQUEST, "Not a vaild token");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new ServiceException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
        }
    }

    public UserModel getAccountByUid(DSLContext ctx, String uid) throws ServiceException {
        AccountRecord account = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().stream().findFirst().orElse(null);
        if (account == null) {
            throw new ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "User not found");
        } else {
            UserModel user = new UserModel();
            user.setUid(account.getUid());
            user.setEmail(account.getEmail());
            user.setLinkbitAddress(account.getAddress());
            user.setName(account.getName());
            user.setProfileUrl(account.getProfile());
            return user;
        }
    }

    public boolean unRegister(DSLContext ctx, String uid) {
        int result = ctx.deleteFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).execute();
        return result > 0;
    }
}
