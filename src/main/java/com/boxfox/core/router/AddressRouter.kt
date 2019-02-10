package com.boxfox.core.router

import com.boxfox.linkbit.service.address.AddressService
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_NOT_MODIFIED
import com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.json.JSONObject

class AddressRouter : AbstractAuthRouter() {

    @Service
    private lateinit var addressService: AddressService

    @RouteRegistration(uri = "/address", method = [HttpMethod.GET], auth = true)
    fun getAddressList(ctx: RoutingContext) {
        addressService.getList(getUid(ctx)).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address/valid", method = [HttpMethod.GET], auth = true)
    fun checkAddressValid(ctx: RoutingContext, @Param(name = "symbol") symbol: String, @Param(name = "address") address: String) {
        addressService.checkAddressValid(symbol, address).subscribe({
            if(it){
                ctx.response().end()
            }else{
                ctx.response().setStatusCode(400).end()
            }
        }, {
            ctx.fail(it)
        })
    }


    @RouteRegistration(uri = "/address/accounts", method = [HttpMethod.GET], auth = true)
    fun getLinkedAddress(ctx: RoutingContext, @Param(name = "address") address: String) {
        addressService.getAddress(address).subscribe({
            ctx.response().end(gson.toJson(it.accountAddressMap))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address/valid", method = [HttpMethod.GET])
    fun checkAddressValid(ctx: RoutingContext, @Param(name = "address") address: String) {
        addressService.checkAddressExist(address).subscribe({
            ctx.response().end(JSONObject().put("result", !it).toString())
        }, { ctx.fail(it) })
    }

    @RouteRegistration(uri = "/address/account", method = [HttpMethod.PUT], auth = true)
    fun registerAddress(ctx: RoutingContext,
                        @Param(name = "linkAddress") linkAddress: String,
                        @Param(name = "symbol") symbol: String,
                        @Param(name = "accountAddress") accountAddress: String) {
        val uid = getUid(ctx)
        addressService.register(uid, linkAddress, symbol, accountAddress).subscribe({
            if (it) {
                endResponse(ctx, STATUS_CODE_OK)
            } else {
                endResponse(ctx, STATUS_CODE_NOT_MODIFIED)
            }
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address/account", method = [HttpMethod.DELETE], auth = true)
    fun unregisterAddress(ctx: RoutingContext,
                          @Param(name = "linkAddress") linkAddress: String,
                          @Param(name = "symbol") symbol: String) {
        addressService.unregister(getUid(ctx), linkAddress, symbol).subscribe({
            if (it) {
                endResponse(ctx, STATUS_CODE_OK)
            } else {
                endResponse(ctx, STATUS_CODE_NOT_MODIFIED)
            }
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/address", method = [HttpMethod.POST], auth = true)
    fun buyAddress(ctx: RoutingContext, @Param(name = "linkAddress") linkAddress: String) {
        val uid = getUid(ctx)
        addressService.newAddress(uid, linkAddress).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }
}