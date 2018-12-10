package com.boxfox.cross.service.address

import com.boxfox.cross.common.entity.AddressModel
import com.boxfox.cross.service.JooqReactiveService
import io.reactivex.Single

class AddressService(private val impl: AddressUsecase = AddressServiceImpl()) : JooqReactiveService() {

    fun getList(uid: String): Single<List<AddressModel>> = createSingle { ctx -> impl.getLinkAddress(ctx, uid) }

    fun register(uid: String, linkAddress: String, symbol: String, accountAddress: String): Single<Boolean> {
        return createSingle{impl.registerAddress(it, uid, linkAddress, symbol, accountAddress)}
    }

    fun unregister(uid: String, linkAddress: String, symbol: String): Single<Boolean> {
        return createSingle{impl.unregisterAddress(it, uid, linkAddress, symbol)}
    }

    fun checkAddressExist(address: String): Single<Boolean> {
        return createSingle{impl.checkAddressExist(it, address)}
    }
}
