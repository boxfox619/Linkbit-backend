package com.boxfox.core.router;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.FriendService;
import com.boxfox.cross.service.model.Profile;
import com.google.gson.Gson;
import io.one.sys.db.tables.daos.AccountDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import static io.one.sys.db.Tables.FRIEND;

public class FriendRouter {

    @Service
    private FriendService friendService;
    private Gson gson;

    public FriendRouter(){
        this.gson = new Gson();
    }

    @RouteRegistration(uri = "/search/account/:type", method = HttpMethod.GET, auth = true)
    public void search(RoutingContext ctx, @Param String type, @Param String text) {
        friendService.serachUsers(type, text, res -> {
            ctx.response().end(new Gson().toJson(res.result()));
        });
    }

    @RouteRegistration(uri = "/friend", method = HttpMethod.GET, auth = true)
    public void loadFriends(RoutingContext ctx) {
        String uid = (String) ctx.data().get("uid");
        friendService.loadFriends(uid, res->{
            ctx.response().end(gson.toJson(res.result()));
        });
    }


    @RouteRegistration(uri = "/friend", method = HttpMethod.PUT, auth = true)
    public void addFriend(RoutingContext ctx, @Param String uid) {
        String ownUid = (String) ctx.data().get("uid");
        friendService.addFriend(ownUid, uid, res->{
            if(res.succeeded()){
                ctx.response().setStatusCode(200).end();
            }else{
                ctx.fail(res.cause());
            }
        });
    }


    @RouteRegistration(uri = "/friend", method = HttpMethod.DELETE, auth = true)
    public void deleteFriend(RoutingContext ctx, @Param String uid) {
        String ownUid = (String) ctx.data().get("uid");
        friendService.deleteFriend(ownUid, uid, res -> {
            if(res.succeeded()){
                ctx.response().setStatusCode(200).end();
            }else{
                ctx.fail(res.cause());
            }
        });
    }


}
