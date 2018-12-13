package com.boxfox.cross.service.address

import com.boxfox.cross.common.entity.Address.AddressModel
import org.jooq.DSLContext

interface AddressUsecase {
    fun getLinkAddress(uid: String): List<AddressModel>
    fun registerAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol:String, accountAddress: String): Boolean
    fun unregisterAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String): Boolean
    fun checkAddressOwn(ctx: DSLContext, uid: String, linkAddress: String): Boolean
    fun checkAddressExist(ctx: DSLContext, address: String): Boolean
}
