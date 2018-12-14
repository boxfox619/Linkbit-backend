package com.boxfox.linkbit.common.entity.Address

import com.boxfox.linkbit.common.entity.EntityMapper
import io.one.sys.db.Tables.ADDRESS
import io.one.sys.db.tables.pojos.Address
import io.one.sys.db.tables.pojos.Linkaddress
import org.jooq.Record

object AddressEntityMapper : EntityMapper<AddressModel, Address> {

    override fun toEntity(v: Address): AddressModel {
        return AddressModel().apply {
            this.linkAddress = v.linkaddress
        }
    }

    override fun toEntity(record: Record): AddressModel {
        return AddressModel().apply {
            this.linkAddress = record.get(ADDRESS.LINKADDRESS)
        }
    }

    fun toEntity(address: AddressModel, linkedAddressList: List<Linkaddress>) : AddressModel {
        linkedAddressList.forEach{
            address.accountAddressMap[it.symbol] = it.accountaddress
        }
        return address
    }

}