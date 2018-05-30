package com.boxfox.cross.service.auth;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.auth.facebook.FacebookAuth;
import io.one.sys.db.tables.daos.AccountDao;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.ACCOUNT;


public class AuthService {

    public boolean signin(String username, String password) {
        return true;
    }

    public JsonObject signinWithFacebook(String accessToken) {
        JsonObject result = new JsonObject();
        AccountDao data = new AccountDao(PostgresConfig.create());
        result.put("result", false);
        Profile profile = FacebookAuth.validation(accessToken);
        if(profile!=null) {
            result.put("result", true);
            result.put("email", profile.getEmail());
            if (data.fetchByEmail(profile.getEmail()).size() == 0) {
                String address = createRandomAddress(data);
                createContext().insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.ADDRESS).values(profile.getUid() , profile.getEmail(), profile.getName(), address);
            }
        }
        return result;
    }

    public static String createToken(Vertx vertx, String username) {
        JWTAuth jwt = createJWTAuth(vertx);
        String token = jwt.generateToken(new JsonObject().put("sub", username), new JWTOptions());
        return token;
    }

    private static String createRandomAddress(AccountDao accountDao){
        String address;
        do {
            int firstNum = (int) (Math.random() * 9999 + 1);
            int secondNum = (int) (Math.random() * 999999 + 1);
            int lastNum = (int) (Math.random() * 99 + 1);
            address = String.format("%04d-%06d-$02d", firstNum, secondNum, lastNum);
        }while(accountDao.fetchByAddress(address).size()>0);
        return address;
    }

    public static JWTAuth createJWTAuth(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("public-key", "KEYCLOAK_PUBLIC_KEY")
                .put("permissionsClaimKey", "realm_access/roles");
        JWTAuth authProvider = JWTAuth.create(vertx, config);
        return authProvider;
    }
}
