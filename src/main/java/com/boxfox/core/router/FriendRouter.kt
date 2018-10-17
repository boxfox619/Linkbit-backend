package com.boxfox.core.router

import com.boxfox.cross.service.friend.FriendService
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext


class FriendRouter : AbstractRouter() {

    @Service
    private lateinit var friendService: FriendService

    @RouteRegistration(uri = "/search/account/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun search(ctx: RoutingContext, @Param(name = "text") text: String) {
        friendService.serachUsers(text).subscribe({ list ->
            ctx.response().end(gson.toJson(list))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/search/account", method = arrayOf(HttpMethod.GET), auth = true)
    fun searchAccount(ctx: RoutingContext, @Param(name = "uid") uid: String) {
        friendService.getUser(uid).subscribe({ user ->
            ctx.response().end(gson.toJson(user))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/friend", method = arrayOf(HttpMethod.GET), auth = true)
    fun loadFriends(ctx: RoutingContext) {
        val uid = ctx.data()["uid"] as String
        friendService.loadFriends(uid).subscribe({ list ->
            ctx.response().end(gson.toJson(list))
        }, {
            ctx.fail(it)
        })
    }


    @RouteRegistration(uri = "/friend", method = arrayOf(HttpMethod.PUT), auth = true)
    fun addFriend(ctx: RoutingContext, @Param(name = "uid") targetUid: String) {
        val ownUid = ctx.data()["uid"] as String
        friendService.addFriend(ownUid, targetUid).subscribe({
            ctx.response().setStatusCode(200).end()
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/friend", method = arrayOf(HttpMethod.DELETE), auth = true)
    fun deleteFriend(ctx: RoutingContext, @Param(name = "uid") targetUid: String) {
        val ownUid = ctx.data()["uid"] as String
        friendService.deleteFriend(ownUid, targetUid).subscribe({
            ctx.response().setStatusCode(200).end()
        }, {
            ctx.fail(it)
        })
    }


}
