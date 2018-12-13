package com.boxfox.core.router

import com.boxfox.cross.service.address.AddressService
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import com.google.api.client.http.HttpStatusCodes
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.json.JSONObject

class AddressRouter : AbstractRouter() {
    @Service private lateinit var addressService: AddressService

    @RouteRegistration(uri = "/address", method = arrayOf(HttpMethod.GET))
    fun getAddressList(ctx: RoutingContext) {
        val uid = ctx.data()["uid"] as String
        addressService.getList(uid).subscribe({
            ctx.response().end(gson.toJson(it))
        },{
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address/valid", method = arrayOf(HttpMethod.GET))
    fun checkAddressValid(ctx: RoutingContext, @Param(name = "address") address: String) {
        addressService.checkAddressExist(address).subscribe({
            ctx.response().end(JSONObject().put("result", !it).toString())
        },{ctx.fail(it)})
    }

    @RouteRegistration(uri = "/address", method = arrayOf(HttpMethod.PUT))
    fun registerAddress(ctx: RoutingContext,
                        @Param(name = "linkAddress") linkAddress: String,
                        @Param(name = "symbol") symbol: String,
                        @Param(name = "accountAddress") accountAddress: String) {
        val uid = ctx.data()["uid"] as String
        addressService.register(uid, linkAddress, symbol, accountAddress).subscribe({
            if (it) {
                ctx.response().end()
            } else {
                ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED).end()
            }
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address", method = arrayOf(HttpMethod.DELETE))
    fun unregisterAddress(ctx: RoutingContext,
                        @Param(name = "linkAddress") linkAddress: String,
                        @Param(name = "symbol") symbol: String) {
        val uid = ctx.data()["uid"] as String
        addressService.unregister(uid, linkAddress, symbol).subscribe({
            if (it) {
                ctx.response().end()
            } else {
                ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED).end()
            }
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address", method = arrayOf(HttpMethod.POST))
    fun buyAddress(ctx: RoutingContext, @Param(name = "linkAddress") linkAddress: String) {
        //@TODO Implement address buy function
    }
}