package com.boxfox.core.router;

import com.boxfox.support.vertx.router.Param;
import com.boxfox.support.vertx.router.RouteRegistration;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class FriendRouter {

    @RouteRegistration(uri = "/search", method = HttpMethod.GET, auth = true)
    public void signin(RoutingContext ctx, @Param String text) {

    }
}
