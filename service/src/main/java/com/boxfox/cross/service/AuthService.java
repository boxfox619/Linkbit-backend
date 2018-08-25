package com.boxfox.cross.service;

import static io.one.sys.db.Tables.ACCOUNT;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.service.AbstractService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.linkbit.android.entity.UserModel;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.pojos.Account;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.concurrent.ExecutionException;


public class AuthService extends AbstractService {

  public void signin(String token, Handler<AsyncResult<UserModel>> hander) {
    doAsync(future -> {
      AccountDao data = new AccountDao(PostgresConfig.create());
      try {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token).get();
        if (decodedToken != null) {
          UserModel user = new UserModel();
          user.setUid(decodedToken.getUid());
          user.setName(decodedToken.getName());
          user.setEmail(decodedToken.getEmail());
          user.setProfileUrl(decodedToken.getPicture());
          if (data.fetchByUid(decodedToken.getUid()).size() == 0) {
            useContext(ctx -> {
              String address = AddressService.createRandomAddress(ctx);
              user.setLinkbitAddress(address);
              ctx.insertInto(ACCOUNT, ACCOUNT.UID, ACCOUNT.EMAIL, ACCOUNT.NAME, ACCOUNT.PROFILE,
                  ACCOUNT.ADDRESS)
                  .values(decodedToken.getUid(), decodedToken.getEmail(), decodedToken.getName(),
                      user.getProfileUrl(), address).execute();
                        /*FacebookAuth.getFriends(accessToken).setHandler(event -> {
                            if (event.succeeded()) {
                                event.result().forEach(p -> create.insertInto(FRIEND).values(profile.getUid(), p.getUid()).execute());
                            }
                        });*/
            });
          } else {
            user.setLinkbitAddress(
                data.fetchByUid(user.getUid()).get(0).getAddress());
          }
          future.complete(user);
        } else {
          future.fail("Not a vaild token");
        }
      } catch (InterruptedException | ExecutionException e) {
        future.fail(e);
      }
    }, hander);
  }

  public UserModel getAccountByUid(String uid) {
    AccountDao dao = new AccountDao(PostgresConfig.create());
    Account account = dao.fetchOneByUid(uid);
    UserModel user = new UserModel();
    user.setUid(account.getUid());
    user.setEmail(account.getEmail());
    user.setName(account.getName());
    return user;
  }
}
