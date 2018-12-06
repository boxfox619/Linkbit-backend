package com.boxfox.cross.service.address

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.entity.AddressModel
import com.google.api.client.http.HttpStatusCodes
import org.jooq.DSLContext

class AddressServiceImpl : AddressUsecase{

    @Throws(RoutingException::class)
    override fun getLinkAddress(ctx: DSLContext, uid: String): List<AddressModel> {
        //@TODO bought address lookup
        throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Not implemented")
    }

    override fun registerAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String, accountAddress: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
