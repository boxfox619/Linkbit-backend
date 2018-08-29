package com.boxfox.core.router;

import com.boxfox.cross.common.util.LogUtil;
import com.boxfox.cross.common.vertx.router.AbstractRouter;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AuthService;
import com.google.gson.Gson;
import com.linkbit.android.entity.UserModel;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AuthRouter extends AbstractRouter {

    @Service
    private AuthService authService;
    private Gson gson;

    public AuthRouter(){
        gson = new Gson();
    }

    @RouteRegistration(uri = "/signin", method = HttpMethod.GET)
    public void signin(RoutingContext ctx, @Param String token) {
        LogUtil.debug("signin token:%s", token);
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

    @RouteRegistration(uri = "/logout", method = HttpMethod.GET, auth = true)
    public void logout(RoutingContext ctx) {
        ctx.removeCookie("token");
        ctx.response().setStatusCode(200).end();
    }
}
