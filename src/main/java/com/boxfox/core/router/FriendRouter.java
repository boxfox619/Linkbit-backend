package com.boxfox.core.router;

import com.boxfox.support.data.PostgresConfig;
import com.boxfox.support.vertx.router.Param;
import com.boxfox.support.vertx.router.RouteRegistration;
import io.one.sys.db.tables.Account;
import io.one.sys.db.tables.daos.AccountDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

import java.util.ArrayList;
import java.util.List;

import static com.boxfox.support.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.FRIEND;
import static io.one.sys.db.tables.Account.ACCOUNT;

public class FriendRouter {

    @RouteRegistration(uri = "/search/account/:type", method = HttpMethod.GET, auth = true)
    public void search(RoutingContext ctx, @Param String type, @Param String text) {
        SelectJoinStep step = createContext().select().from(ACCOUNT);
        SelectConditionStep conditionStep = null;
        switch (type) {
            case "address":
                conditionStep = step.where(ACCOUNT.ADDRESS.like(text));
                break;
            case "name":
                conditionStep = step.where(ACCOUNT.NAME.like(text));
                break;
            default:
                conditionStep = step.where(ACCOUNT.EMAIL.like(text));
        }
        JsonArray accounts = new JsonArray();
        conditionStep.fetch().map(r -> new JsonObject(r.formatJSON())).forEach(r -> accounts.add(r));
        JsonObject result = new JsonObject();
        result.put("result", true);
        result.put("accounts", accounts);
        ctx.response().end(result.encode());
    }

    @RouteRegistration(uri = "/friend/", method = HttpMethod.PUT, auth = true)
    public void addFriend(RoutingContext ctx, @Param String uid) {
        AccountDao dao = new AccountDao(PostgresConfig.create());
        int statusCode = 400;
        String errorMessage = null;
        if (dao.fetchOneByUid(uid) == null) {
            errorMessage = "Target user can not found";
        } else {
            int result = createContext().insertInto(FRIEND, FRIEND.UID, FRIEND.FRIEND_).values(uid, uid).execute();
            if (result == 1) {
                statusCode = 200;
            } else {
                errorMessage = "Update fail";
            }
        }
        ctx.response().setStatusMessage(errorMessage).setStatusCode(statusCode).end();
    }


    @RouteRegistration(uri = "/friend/", method = HttpMethod.DELETE, auth = true)
    public void deleteFriend(RoutingContext ctx, @Param String uid) {
        AccountDao dao = new AccountDao(PostgresConfig.create());
        int statusCode = 400;
        String errorMessage = null;
        if (dao.fetchOneByUid(uid) == null) {
            errorMessage = "Target user can not found";
        } else {
            int result = createContext()
                    .deleteFrom(FRIEND)
                    .where(FRIEND.UID.equal(uid).and(FRIEND.FRIEND_.equal(uid))).execute();
            if (result == 1) {
                statusCode = 200;
            } else {
                errorMessage = "Update fail";
            }
        }
        ctx.response().setStatusMessage(errorMessage).setStatusCode(statusCode).end();
    }


}
