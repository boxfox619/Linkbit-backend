package com.boxfox.cross.service;

import static io.one.sys.db.Tables.ACCOUNT;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.JWTAuthUtil;
import com.boxfox.cross.common.vertx.service.AbstractService;
import com.boxfox.cross.service.model.Profile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.pojos.Account;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class AuthService extends AbstractService {

    public void init(){
        try {
            System.out.println("Initilize firebase");
            FileInputStream serviceAccount = new FileInputStream("keystore/cross-c863f-3861d7d0cc90.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void signin(String token, Handler<AsyncResult<Profile>> hander){
        doAsync(future -> {
            AccountDao data = new AccountDao(PostgresConfig.create());
            try {
                FirebaseToken decodedToken  = FirebaseAuth.getInstance().verifyIdTokenAsync(token).get();
                if(decodedToken!=null){
                    Profile profile = new Profile();
                    profile.setUid(decodedToken.getUid());
                    profile.setName(decodedToken.getName());
                    profile.setEmail(decodedToken.getEmail());
                    profile.setProfile(decodedToken.getPicture());
                    if (data.fetchByUid(decodedToken.getUid()).size() == 0) {
                        useContext(ctx -> {
                            String address = AddressService.createRandomAddress(ctx);
                            profile.setCrossAddress(address);
                            ctx.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.ADDRESS).values(decodedToken.getUid(), decodedToken.getEmail(), decodedToken.getName(), address).execute();
                        /*FacebookAuth.getFriends(accessToken).setHandler(event -> {
                            if (event.succeeded()) {
                                event.result().forEach(p -> create.insertInto(FRIEND).values(profile.getUid(), p.getUid()).execute());
                            }
                        });*/
                        });
                    } else {
                        profile.setCrossAddress(
                                data.fetchByUid(profile.getUid()).get(0).getAddress());
                    }
                    future.complete(profile);
                }else{
                    future.fail("Not a vaild token");
                }
            } catch (InterruptedException | ExecutionException e) {
                future.fail(e);
            }
        }, hander);
    }

    public String createJWT(Vertx vertx, String firebaseToken){
        String token = null;
        try {
            FirebaseToken decodedToken  = FirebaseAuth.getInstance().verifyIdTokenAsync(firebaseToken).get();
            JsonObject obj = new JsonObject(decodedToken.getClaims());
            token = JWTAuthUtil.createAuth(vertx).generateToken(obj, new JWTOptions());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return token;
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
