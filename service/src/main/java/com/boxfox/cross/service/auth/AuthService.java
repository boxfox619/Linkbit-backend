package com.boxfox.cross.service.auth;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.address.AddressService;
import com.boxfox.cross.service.auth.facebook.FacebookAuth;
import io.one.sys.db.tables.daos.AccountDao;
import org.jooq.DSLContext;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.ACCOUNT;
import static io.one.sys.db.tables.Friend.FRIEND;


public class AuthService {

    public boolean signin(String username, String password) {
        return true;
    }

    public Profile signinWithFacebook(String accessToken) {
        AccountDao data = new AccountDao(PostgresConfig.create());
        Profile profile = FacebookAuth.validation(accessToken);
        if(profile!=null) {
            if (data.fetchByUid(profile.getUid()).size() == 0) {
                String address = AddressService.createRandomAddress(data);
                DSLContext create = createContext();
                create.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.ADDRESS)
                        .values(profile.getUid(), profile.getEmail(), profile.getName(), address)
                        .execute();
                FacebookAuth
                        .getFriends(accessToken)
                        .forEach(p -> create.insertInto(FRIEND).values(profile.getUid(), p.getUid()).execute());
            }
        }
        return profile;
    }
}
