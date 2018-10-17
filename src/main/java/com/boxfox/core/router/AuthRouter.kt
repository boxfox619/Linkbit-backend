package com.boxfox.core.router

import com.boxfox.vertx.router.*
import com.boxfox.vertx.service.*
import com.boxfox.cross.service.auth.AuthService
import com.google.api.client.http.HttpStatusCodes
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.apache.log4j.Logger

import com.boxfox.cross.util.LogUtil.getLogger

class AuthRouter : AbstractRouter() {

    @Service
    private lateinit var authService: AuthService

    @RouteRegistration(uri = "/auth", method = arrayOf(HttpMethod.POST))
    fun signin(ctx: RoutingContext, @Param(name = "token") token: String) {
        getLogger().debug(String.format("signin token: %s", token))
        authService.signin(token).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/auth/logout", method = arrayOf(HttpMethod.GET), auth = true)
    fun logout(ctx: RoutingContext) {
        ctx.removeCookie("token")
        ctx.response().setStatusCode(200).end()
    }

    @RouteRegistration(uri = "/auth", method = arrayOf(HttpMethod.GET), auth = true)
    fun info(ctx: RoutingContext) {
        val uid = ctx.data()["uid"] as String
        this.authService.getAccountByUid(uid).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/auth", method = arrayOf(HttpMethod.DELETE), auth = true)
    fun unRegister(ctx: RoutingContext) {
        val uid = ctx.data()["uid"].toString()
        Logger.getRootLogger().info(String.format("user delete %s", uid))
        this.authService.unRegister(uid).subscribe({
            ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_OK).end()
        }, {
            ctx.fail(it)
        })
    }
}
