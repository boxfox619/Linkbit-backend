package com.boxfox.core.router;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.model.Profile;
import com.google.gson.Gson;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.pojos.Account;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.jooq.SelectJoinStep;

import java.util.List;
import java.util.stream.Collectors;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.FRIEND;
import static io.one.sys.db.tables.Account.ACCOUNT;

public class FriendRouter {

    @RouteRegistration(uri = "/search/account/:type", method = HttpMethod.GET, auth = true)
    public void search(RoutingContext ctx, @Param String type, @Param String text) {
        SelectJoinStep step = createContext().select().from(ACCOUNT);
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
        ctx.response().end(new Gson().toJson(profileList));
    }

    @RouteRegistration(uri = "/friend/", method = HttpMethod.GET, auth = true)
    public void loadFriends(RoutingContext ctx) {
        new Thread(() -> {
            String uid = (String) ctx.data().get("uid");
            JsonArray friends = new JsonArray();
            AccountDao dao = new AccountDao(PostgresConfig.create());
            createContext().selectFrom(FRIEND).where(FRIEND.UID.eq(uid).or(FRIEND.FRIEND_.eq(uid))).fetch().forEach(r -> {
                String target = r.getFriend().equals(uid) ? r.getUid() : r.getFriend();
                Account acc = dao.fetchOneByUid(target);
                Profile profile = new Profile();
                profile.setName(acc.getName());
                profile.setEmail(acc.getEmail());
                profile.setUid(acc.getUid());
                friends.add(new JsonObject(new Gson().toJson(profile)));
            });
            ctx.response().end(friends.encode());
        }).start();
    }


    @RouteRegistration(uri = "/friend/", method = HttpMethod.PUT, auth = true)
    public void addFriend(RoutingContext ctx, @Param String uid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AccountDao dao = new AccountDao(PostgresConfig.create());
                String ownUid = (String) ctx.data().get("uid");
                int statusCode = 400;
                String errorMessage = null;
                if (dao.fetchOneByUid(uid) == null) {
                    errorMessage = "Target user can not found";
                } else {
                    int result = createContext().insertInto(FRIEND, FRIEND.UID, FRIEND.FRIEND_).values(ownUid, uid).execute();
                    if (result == 1) {
                        statusCode = 200;
                    } else {
                        errorMessage = "Update fail";
                    }
                }
                ctx.response().setStatusMessage(errorMessage).setStatusCode(statusCode).end();
            }
        }).start();
    }


    @RouteRegistration(uri = "/friend/", method = HttpMethod.DELETE, auth = true)
    public void deleteFriend(RoutingContext ctx, @Param String uid) {
        AccountDao dao = new AccountDao(PostgresConfig.create());
        String ownUid = (String) ctx.data().get("uid");
        int statusCode = 400;
        String errorMessage = null;
        if (dao.fetchOneByUid(uid) == null) {
            errorMessage = "Target user can not found";
        } else {
            int result = createContext()
                    .deleteFrom(FRIEND)
                    .where(FRIEND.UID.equal(ownUid).and(FRIEND.FRIEND_.equal(uid))).execute();
            if (result == 1) {
                statusCode = 200;
            } else {
                errorMessage = "Update fail";
            }
        }
        ctx.response().setStatusMessage(errorMessage).setStatusCode(statusCode).end();
    }


}
