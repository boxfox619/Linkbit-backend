package com.boxfox.cross.service;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.service.AbstractService;
import com.boxfox.cross.service.model.Profile;
import com.google.gson.Gson;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.pojos.Account;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.one.sys.db.Tables.FRIEND;


public class FriendService extends AbstractService{

    public void loadFriends(String uid, Handler<AsyncResult<List<Profile>>> res){
        doAsync(future -> {
            List<Profile> friends = new ArrayList();
            AccountDao dao = new AccountDao(PostgresConfig.create());
            useContext(ctx -> {
                ctx.selectFrom(FRIEND).where(FRIEND.UID_1.eq(uid).or(FRIEND.UID_2.eq(uid))).fetch().forEach(r -> {
                    String target = r.getUid_1().equals(uid) ? r.getUid_2() : r.getUid_1();
                    Account acc = dao.fetchOneByUid(target);
                    Profile profile = new Profile();
                    profile.setName(acc.getName());
                    profile.setEmail(acc.getEmail());
                    profile.setUid(acc.getUid());
                    profile.setCrossAddress(acc.getAddress());
                    friends.add(profile);
                });
            });
            future.complete(friends);
        },res);
    }

    public void addFriend(String ownUid, String uid, Handler<AsyncResult<Void>> res){
        doAsync(future -> {
            AccountDao dao = new AccountDao(PostgresConfig.create());
            if (dao.fetchOneByUid(uid) == null) {
                future.fail("Target user can not found");
            } else {
                useContext(ctx->{
                    int result = ctx.insertInto(FRIEND, FRIEND.UID_1, FRIEND.UID_2).values(ownUid, uid).execute();
                    if (result == 1) {
                        future.complete();
                    } else {
                        future.fail("Update fail");
                    }
                });
            }
        },res);
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

    public void serachUsers(String type, String text, Handler<AsyncResult<List<Profile>>> res){
        doAsync(future -> {
            AccountDao dao = new AccountDao(PostgresConfig.create());
            List<Account> accounts = null;
            switch (type) {
                case "address":
                    accounts = dao.fetchByAddress(text);
                    break;
                case "name":
                    accounts = dao.fetchByName(text);
                    break;
                default:
                    accounts = dao.fetchByEmail(text);
            }
            List<Profile> profileList = accounts.stream().map(a -> {
                Profile profile = new Profile();
                profile.setUid(a.getUid());
                profile.setEmail(a.getUid());
                profile.setName(a.getName());
                return profile;
            }).collect(Collectors.toList());
            future.complete(profileList);
        }, res);
    }
}
