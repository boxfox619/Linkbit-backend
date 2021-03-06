package com.boxfox.linkbit.service.address

import com.boxfox.linkbit.common.entity.Address.AddressModel
import com.boxfox.linkbit.service.JooqReactiveService
import io.reactivex.Single

class AddressService(private val impl: AddressUsecase = AddressServiceImpl()) : JooqReactiveService() {

    fun newAddress(uid: String, address: String): Single<AddressModel> = single { impl.createLinkAddress(uid, address)}

    fun getList(uid: String): Single<List<AddressModel>> = single { impl.getLinkAddressList(uid) }

    fun getAddress(linkAddress: String): Single<AddressModel> = single { impl.getLinkAddress(linkAddress)}

    fun register(uid: String, linkAddress: String, symbol: String, accountAddress: String): Single<Boolean> {
        return single{impl.registerAddress(it, uid, linkAddress, symbol, accountAddress)}
    }

    fun unregister(uid: String, linkAddress: String, symbol: String): Single<Boolean> {
        return single{impl.unregisterAddress(it, uid, linkAddress, symbol)}
    }

    fun checkAddressExist(address: String): Single<Boolean> {
        return single{impl.checkAddressExist(it, address)}
    }

    fun checkAddressValid(symbol: String, address: String): Single<Boolean> {
        return single{impl.checkAddressValid(it, symbol, address)}
    }
}
