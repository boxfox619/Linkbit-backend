package com.boxfox.linkbit.service.address

import com.boxfox.linkbit.common.entity.Address.AddressModel
import org.jooq.DSLContext

interface AddressUsecase {
    fun getLinkAddressList(uid: String): List<AddressModel>
    fun getLinkAddress(address: String): AddressModel
    fun registerAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol:String, accountAddress: String): Boolean
    fun unregisterAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String): Boolean
    fun checkAddressOwn(ctx: DSLContext, uid: String, linkAddress: String): Boolean
    fun checkAddressExist(ctx: DSLContext, address: String): Boolean
    fun registerRandomAddress(ctx: DSLContext, uid: String, symbol: String, originalAddress: String): Boolean
}
