package com.boxfox.linkbit.service.address

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.common.data.PostgresConfig
import com.boxfox.linkbit.common.entity.Address.AddressEntityMapper
import com.boxfox.linkbit.common.entity.Address.AddressModel
import io.one.sys.db.Tables.ADDRESS
import io.one.sys.db.Tables.LINKADDRESS
import io.one.sys.db.tables.daos.AddressDao
import io.one.sys.db.tables.daos.LinkaddressDao
import org.jooq.DSLContext

class AddressServiceImpl : com.boxfox.linkbit.service.address.AddressUsecase {
    private val addressDao = AddressDao(PostgresConfig.create())
    private val linkAddressDao = LinkaddressDao(PostgresConfig.create())

    @Throws(RoutingException::class)
    override fun getLinkAddressList(uid: String): List<AddressModel> {
        return addressDao.fetchByUid(uid)
                .map { AddressEntityMapper.toEntity(it) }
                .map { AddressEntityMapper.toEntity(it, linkAddressDao.fetchByLinkaddress(it.linkAddress)) }
    }

    override fun getLinkAddress(address: String): AddressModel {
        return AddressEntityMapper.toEntity(addressDao.fetchOneByLinkaddress(address))
    }

    override fun checkAddressOwn(ctx: DSLContext, uid: String, linkAddress: String): Boolean {
        val count = ctx.selectFrom(ADDRESS).where(ADDRESS.UID.eq(uid).and(ADDRESS.LINKADDRESS.eq(linkAddress))).count()
        return (count > 0)
    }

    override fun registerAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String, accountAddress: String): Boolean {
        var result = 0
        val own = checkAddressOwn(ctx, uid, linkAddress)
        val canLink = !checkSymbolLinked(ctx, linkAddress, symbol.toUpperCase())
        if (own && canLink) {
            result = ctx.insertInto(LINKADDRESS).values(linkAddress, symbol.toUpperCase(), accountAddress).execute()
        }
        return (result > 0)
    }

    override fun unregisterAddress(ctx: DSLContext, uid: String, linkAddress: String, symbol: String): Boolean {
        var result = 0
        if (checkAddressOwn(ctx, uid, linkAddress)) {
            result = ctx.deleteFrom(LINKADDRESS).where(LINKADDRESS.LINKADDRESS_.eq(linkAddress).and(LINKADDRESS.SYMBOL.eq(symbol.toUpperCase()))).execute()
        }
        return (result > 0)
    }

    fun checkSymbolLinked(ctx: DSLContext, linkAddress: String, symbol: String): Boolean {
        val count = ctx.selectFrom(LINKADDRESS).where(
                LINKADDRESS.LINKADDRESS_.eq(linkAddress).and(LINKADDRESS.SYMBOL.eq(symbol.toUpperCase())))
                .count()
        return (count>0)
    }

    override fun checkAddressExist(ctx: DSLContext, address: String): Boolean {
        val count = ctx.selectFrom(ADDRESS).where(ADDRESS.LINKADDRESS.eq(address)).count()
        return (count > 0)
    }

}
