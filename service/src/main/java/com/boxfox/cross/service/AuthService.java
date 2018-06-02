package com.boxfox.cross.service;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.model.Profile;
import com.boxfox.cross.service.auth.facebook.FacebookAuth;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.pojos.Account;
import io.vertx.core.Future;
import org.jooq.DSLContext;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.ACCOUNT;
import static io.one.sys.db.tables.Friend.FRIEND;


public class AuthService {

    public boolean signin(String username, String password) {
        return true;
    }

    public Future<Profile> signinWithFacebook(String accessToken) {
        Future<Profile> future = Future.future();
        AccountDao data = new AccountDao(PostgresConfig.create());
        FacebookAuth.validation(accessToken).setHandler(e -> {
            if(e.succeeded()) {
                Profile profile = e.result();
                if (data.fetchByUid(profile.getUid()).size() == 0) {
                    String address = AddressService.createRandomAddress(data);
                    DSLContext create = createContext();
                    create.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.ADDRESS)
                            .values(profile.getUid(), profile.getEmail(), profile.getName(), address)
                            .execute();
                    FacebookAuth.getFriends(accessToken).setHandler(event -> {
                        if (event.succeeded()) {
                            event.result().forEach(p -> create.insertInto(FRIEND).values(profile.getUid(), p.getUid()).execute());
                        }
                    });
                }
                future.complete(profile);
            }else{
                future.fail(e.cause());
            }
        });
        return future;
    }

    public Profile getAccountByUid(String uid){
        AccountDao dao = new AccountDao(PostgresConfig.create());
        Account account = dao.fetchOneByUid(uid);
        Profile profile = new Profile();
        profile.setUid(account.getUid());
        profile.setEmail(account.getEmail());
        profile.setName(account.getName());
        return profile;
    }
}
