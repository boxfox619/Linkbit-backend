package com.boxfox.cross.service.auth;

import com.boxfox.cross.service.AddressService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.linkbit.android.entity.UserModel;
import io.one.sys.db.tables.records.AccountRecord;
import org.jooq.DSLContext;
import org.jooq.Result;

import static io.one.sys.db.Tables.ACCOUNT;

public class AuthServiceImpl {

    public UserModel signin(DSLContext ctx, String token) throws Throwable {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token).get();
        if (decodedToken != null) {
            UserModel user = new UserModel();
            user.setUid(decodedToken.getUid());
            user.setName(decodedToken.getName());
            user.setEmail(decodedToken.getEmail());
            user.setProfileUrl(decodedToken.getPicture());
            Result<AccountRecord> result = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(user.getUid())).fetch();
            if (result.size() == 0) {
                String address = AddressService.createRandomAddress(ctx);
                user.setLinkbitAddress(address);
                ctx.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.PROFILE, ACCOUNT.ADDRESS)
                        .values(decodedToken.getUid(), decodedToken.getEmail(), decodedToken.getName(), user.getProfileUrl(), address).execute();
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
            throw new Throwable("Not a vaild token");
        }
    }

    public UserModel getAccountByUid(DSLContext ctx, String uid) throws Throwable {
        AccountRecord account = ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().stream().findFirst().orElse(null);
        if (account == null) {
            throw new Throwable("User not found");
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
