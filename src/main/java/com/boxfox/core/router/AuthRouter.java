package com.boxfox.core.router;

import com.boxfox.cross.common.vertx.JWTAuthUtil;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.AuthService;
import com.boxfox.cross.service.model.Profile;
import com.google.gson.Gson;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

public class AuthRouter {
    private AuthService authService;
    private Gson gson;

    public AuthRouter(){
        authService = new AuthService();
        gson = new Gson();
    }

    @RouteRegistration(uri = "/signin/", method = HttpMethod.POST)
    public void signinFacebook(RoutingContext ctx, @Param String accessToken) {
        authService.signin(accessToken).setHandler(e -> {
            if (e.succeeded()) {
                Profile result = e.result();
                String token = authService.createJWT(ctx.vertx(), accessToken);
                JsonObject jsonObject = new JsonObject(gson.toJson(result));
                jsonObject.put("token", token);
                ctx.addCookie(Cookie.cookie("token", token));
                ctx.response().end(jsonObject.encode());
            } else {
                ctx.fail(401);
            }
        });
    }

    @RouteRegistration(uri = "/logout", method = HttpMethod.POST, auth = true)
    public void logout(RoutingContext ctx) {
        ctx.removeCookie("token");
        ctx.response().setStatusCode(200).end();
    }
}
