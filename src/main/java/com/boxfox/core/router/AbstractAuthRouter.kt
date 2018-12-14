package com.boxfox.core.router

import com.boxfox.vertx.router.AbstractRouter
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

abstract class AbstractAuthRouter : AbstractRouter() {

    fun getUid(ctx: RoutingContext): String {
        val uid = ctx.data().getOrDefault("uid", null)
                ?: throw NullPointerException("uid is not contain in context")
        return uid as String
    }

    fun endResponse(ctx: RoutingContext, code: Int){
        val status = HttpResponseStatus.valueOf(code)
        ctx.response().statusCode = status.code()
        ctx.response().statusMessage = status.reasonPhrase()
        ctx.response().end()
    }
}