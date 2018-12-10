package com.boxfox.cross.service.address

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.entity.AddressModel
import io.one.sys.db.Tables.ADDRESS
import io.one.sys.db.Tables.LINKADDRESS
import org.jooq.DSLContext

class AddressServiceImpl : AddressUsecase {
    @Throws(RoutingException::class)
    override fun getLinkAddress(ctx: DSLContext, uid: String): List<AddressModel> {
        val map = HashMap<String, AddressModel>()
        ctx.selectFrom(ADDRESS.join(LINKADDRESS).on(ADDRESS.LINKADDRESS.eq(LINKADDRESS.LINKADDRESS_)))
                .where(ADDRESS.UID.eq(uid))
                .fetch()
                .forEach {
                    val linkAddress = it.get(ADDRESS.LINKADDRESS)
                    val symbol = it.get(LINKADDRESS.SYMBOL)
                    val accountAddress = it.get(LINKADDRESS.ACCOUNTADDRESS)
                    val addressModel = map.getOrDefault(linkAddress, AddressModel().apply { this.linkAddress = linkAddress })
                    addressModel.accountAddress.put(symbol, accountAddress)
                    map.put(linkAddress, addressModel)
                }
        return map.values.toList()
    }

    override fun checkAddressOwn(ctx: DSLContext, uid: String, linkAddress: String): Boolean {
        val count = ctx.selectFrom(ADDRESS).where(ADDRESS.UID.eq(uid).and(ADDRESS.LINKADDRESS.eq(linkAddress))).fetchCount()
        return (count > 0)
    }

    override fun registerAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String, accountAddress: String): Boolean {
        var result = 0
        if (checkAddressOwn(ctx, uid, linkAddress)) {
            result = ctx.insertInto(LINKADDRESS).values(linkAddress, symbol, accountAddress).execute()
        }
        return (result > 0)
    }

    override fun unregisterAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String): Boolean {
        var result = 0
        if (checkAddressOwn(ctx, uid, linkAddress)) {
            result = ctx.deleteFrom(LINKADDRESS).where(LINKADDRESS.LINKADDRESS_.eq(linkAddress).and(LINKADDRESS.SYMBOL.eq(symbol))).execute()
        }
        return (result > 0)
    }

    override fun checkAddressExist(ctx: DSLContext, address: String): Boolean {
        val count = ctx.selectFrom(ADDRESS).where(ADDRESS.LINKADDRESS.eq(address)).count()
        return (count > 0)
    }

}
