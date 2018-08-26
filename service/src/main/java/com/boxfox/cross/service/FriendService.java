package com.boxfox.cross.service;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.service.AbstractService;
import com.linkbit.android.entity.UserModel;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.pojos.Account;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;

import static io.one.sys.db.Tables.ACCOUNT;
import static io.one.sys.db.Tables.FRIEND;
import static io.one.sys.db.Tables.WALLET;


public class FriendService extends AbstractService {

    public void loadFriends(String uid, Handler<AsyncResult<List<UserModel>>> res) {
        doAsync(future -> {
            List<UserModel> friends = new ArrayList();
            AccountDao dao = new AccountDao(PostgresConfig.create());
            useContext(ctx -> {
                ctx.selectFrom(FRIEND).where(FRIEND.UID_1.eq(uid).or(FRIEND.UID_2.eq(uid))).fetch().forEach(r -> {
                    String target = r.getUid_1().equals(uid) ? r.getUid_2() : r.getUid_1();
                    Account acc = dao.fetchOneByUid(target);
                    UserModel user = new UserModel();
                    user.setName(acc.getName());
                    user.setEmail(acc.getEmail());
                    user.setProfileUrl(acc.getProfile());
                    user.setUid(acc.getUid());
                    user.setLinkbitAddress(acc.getAddress());
                    friends.add(user);
                });
            });
            future.complete(friends);
        }, res);
    }

    public void getUser(String uid, Handler<AsyncResult<UserModel>> res) {
        doAsync(future -> {
            AccountDao dao = new AccountDao(PostgresConfig.create());
            useContext(ctx -> {
                ctx.selectFrom(ACCOUNT).where(ACCOUNT.UID.eq(uid)).fetch().forEach(r -> {
                    if (r.size() > 0) {
                        Account acc = (Account) r.get(0);
                        UserModel user = new UserModel();
                        user.setName(acc.getName());
                        user.setEmail(acc.getEmail());
                        user.setProfileUrl(acc.getProfile());
                        user.setUid(acc.getUid());
                        user.setLinkbitAddress(acc.getAddress());
                        future.complete(user);
                    }
                });
            });
            if (!future.isComplete()) {
                future.fail("User not found");
            }
        }, res);
    }

    public void addFriend(String ownUid, String uid, Handler<AsyncResult<Void>> res) {
        doAsync(future -> {
            AccountDao dao = new AccountDao(PostgresConfig.create());
            if (dao.fetchOneByUid(uid) == null) {
                future.fail("Target user can not found");
            } else {
                useContext(ctx -> {
                    int result = ctx.insertInto(FRIEND, FRIEND.UID_1, FRIEND.UID_2).values(ownUid, uid).execute();
                    if (result == 1) {
                        future.complete();
                    } else {
                        future.fail("Update fail");
                    }
                });
            }
        }, res);
    }

    public void deleteFriend(String ownUid, String uid, Handler<AsyncResult<Void>> res) {
        doAsync(future -> {
            AccountDao dao = new AccountDao(PostgresConfig.create());
            if (dao.fetchOneByUid(uid) == null) {
                future.fail("Target user can not found");
            } else {
                useContext(ctx -> {
                    int result = ctx
                            .deleteFrom(FRIEND)
                            .where(FRIEND.UID_1.equal(ownUid).and(FRIEND.UID_2.equal(uid))).execute();
                    if (result == 1) {
                        future.complete();
                    } else {
                        future.fail("Update fail");
                    }
                });
            }
        }, res);
    }

    public void serachUsers(String text, Handler<AsyncResult<List<UserModel>>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                List<UserModel> accounts = new ArrayList();
                Result<Record> records = ctx.selectFrom(ACCOUNT.join(WALLET).on(ACCOUNT.UID.eq(WALLET.UID)))
                        .where(
                                ACCOUNT.ADDRESS.like(text)
                                        .or(WALLET.ADDRESS.like(text))
                                        .or(WALLET.CROSSADDRESS.like(text))
                                        .or(ACCOUNT.EMAIL.like(text))
                                        .or(ACCOUNT.NAME.like(text))
                        ).fetch();
                records.forEach(r -> {
                    UserModel user = new UserModel();
                    user.setName(r.getValue(ACCOUNT.NAME));
                    user.setEmail(r.getValue(ACCOUNT.EMAIL));
                    user.setLinkbitAddress(r.getValue(ACCOUNT.ADDRESS));
                    user.setUid(r.getValue(ACCOUNT.UID));
                    user.setProfileUrl(r.getValue(ACCOUNT.PROFILE));
                    accounts.add(user);
                });
                future.complete(accounts);
            });

        }, res);
    }
}
