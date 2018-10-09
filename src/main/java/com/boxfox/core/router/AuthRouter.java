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
import static com.boxfox.cross.util.LogUtil.getLogger;

public class AuthRouter extends AbstractRouter {

    @Service
    private AuthService authService;
    private Gson gson;

    public AuthRouter(){
        gson = new Gson();
    }

    @RouteRegistration(uri = "/signin", method = HttpMethod.GET)
    public void signin(RoutingContext ctx, @Param(name="token") String token) {
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

    @RouteRegistration(uri = "/logout", method = HttpMethod.GET, auth = true)
    public void logout(RoutingContext ctx) {
        ctx.removeCookie("token");
        ctx.response().setStatusCode(200).end();
    }

    @RouteRegistration(uri = "/auth/info", method = HttpMethod.GET, auth = true)
    public void info(RoutingContext ctx) {
        String uid = (String)ctx.data().get("uid");
        this.authService.getAccountByUid(uid, res -> {
            if(res.succeeded()){
                ctx.response().end(gson.toJson(res));
            }else{
                ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND).end();
            }
        });
    }
}
