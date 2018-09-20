package com.boxfox.core.router;

import com.boxfox.vertx.vertx.router.*;
import com.boxfox.vertx.vertx.service.*;
import com.boxfox.cross.service.FriendService;
import com.google.gson.Gson;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;


public class FriendRouter extends AbstractRouter {

    @Service
    private FriendService friendService;
    private Gson gson;

    public FriendRouter() {
        this.gson = new Gson();
    }

    @RouteRegistration(uri = "/search/account/list", method = HttpMethod.GET, auth = true)
    public void search(RoutingContext ctx, @Param String text) {
        friendService.serachUsers(text, res -> {
            ctx.response().end(gson.toJson(res.result()));
        });
    }

    @RouteRegistration(uri = "/search/account", method = HttpMethod.GET, auth = true)
    public void searchAccount(RoutingContext ctx, @Param String uid) {
        friendService.getUser(uid, res -> {
            ctx.response().end(gson.toJson(res.result()));
        });
    }

    @RouteRegistration(uri = "/friend", method = HttpMethod.GET, auth = true)
    public void loadFriends(RoutingContext ctx) {
        String uid = (String) ctx.data().get("uid");
        friendService.loadFriends(uid, res -> {
            ctx.response().end(gson.toJson(res.result()));
        });
    }


    @RouteRegistration(uri = "/friend", method = HttpMethod.PUT, auth = true)
    public void addFriend(RoutingContext ctx, @Param String targetUid) {
        String ownUid = (String) ctx.data().get("uid");
        friendService.addFriend(ownUid, targetUid, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200).end();
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    @RouteRegistration(uri = "/friend", method = HttpMethod.DELETE, auth = true)
    public void deleteFriend(RoutingContext ctx, @Param String targetUid) {
        String ownUid = (String) ctx.data().get("uid");
        friendService.deleteFriend(ownUid, targetUid, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200).end();
            } else {
                ctx.fail(res.cause());
            }
        });
    }


}
