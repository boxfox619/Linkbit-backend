package com.boxfox.cross.service.address

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.data.PostgresConfig
import com.boxfox.cross.common.entity.Address.AddressEntityMapper
import com.boxfox.cross.common.entity.Address.AddressModel
import io.one.sys.db.Tables.ADDRESS
import io.one.sys.db.Tables.LINKADDRESS
import io.one.sys.db.tables.daos.AddressDao
import io.one.sys.db.tables.daos.LinkaddressDao
import org.jooq.DSLContext

class AddressServiceImpl : AddressUsecase {
    private val addressDao = AddressDao(PostgresConfig.create())
    private val linkAddressDao = LinkaddressDao(PostgresConfig.create())

    @Throws(RoutingException::class)
    override fun getLinkAddress(uid: String): List<AddressModel> {
        return addressDao.fetchByUid(uid)
                .map{AddressEntityMapper.toEntity(it)}
                .map { AddressEntityMapper.toEntity(it, linkAddressDao.fetchByLinkaddress(it.linkAddress)) }
    }

    override fun checkAddressOwn(ctx: DSLContext, uid: String, linkAddress: String): Boolean {
        val count = ctx.selectFrom(ADDRESS).where(ADDRESS.UID.eq(uid).and(ADDRESS.LINKADDRESS.eq(linkAddress))).count()
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
