package com.boxfox.core.router;


import com.boxfox.vertx.router.*;
import com.boxfox.vertx.service.*;
import com.boxfox.cross.service.AuthService;
import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.linkbit.android.entity.UserModel;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.log4j.Logger;

import static com.boxfox.cross.util.LogUtil.getLogger;

public class AuthRouter extends AbstractRouter {

    @Service
    private AuthService authService;
    private Gson gson;

    public AuthRouter() {
        gson = new Gson();
    }

    @RouteRegistration(uri = "/auth", method = HttpMethod.POST)
    public void signin(RoutingContext ctx, @Param(name = "token") String token) {
        getLogger().debug(String.format("signin token: %s", token));
        authService.signin(token, res -> {
            if (res.succeeded()) {
                UserModel result = res.result();
                JsonObject jsonObject = new JsonObject(gson.toJson(result));
                ctx.response().end(jsonObject.encode());
            } else {
                ctx.fail(401);
            }
        });
    }

    @RouteRegistration(uri = "/auth/logout", method = HttpMethod.GET, auth = true)
    public void logout(RoutingContext ctx) {
        ctx.removeCookie("token");
        ctx.response().setStatusCode(200).end();
    }

    @RouteRegistration(uri = "/auth", method = HttpMethod.GET, auth = true)
    public void info(RoutingContext ctx) {
        String uid = (String) ctx.data().get("uid");
        this.authService.getAccountByUid(uid, res -> {
            if (res.succeeded()) {
                ctx.response().end(gson.toJson(res));
            } else {
                ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND).end();
            }
        });
    }


    @RouteRegistration(uri = "/auth", method = HttpMethod.DELETE, auth = true)
    public void unRegister(RoutingContext ctx) {
        String uid = ctx.data().get("uid").toString();
        Logger.getRootLogger().info(String.format("user delete %s", uid));
        this.authService.unRegister(uid, res -> {
            if (res.succeeded()) {
                int code = res.result() ? HttpStatusCodes.STATUS_CODE_OK : HttpStatusCodes.STATUS_CODE_SERVER_ERROR;
                ctx.response().setStatusCode(code).end();
            } else {
                ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR).end();
            }
        });
    }
}
