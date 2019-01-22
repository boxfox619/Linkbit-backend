package com.boxfox.linkbit.service.withdraw

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.util.AddressUtil
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.boxfox.linkbit.wallet.model.TransactionResult
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.Tables.WALLET
import io.vertx.core.json.JsonObject
import org.jooq.DSLContext

class WithdrawServiceImpl : WithdrawUsecase {
    override fun withdraw(ctx: DSLContext, symbol: String, walletData: JsonObject, targetAddress: String, amount: String): TransactionResult {
        var destAddress: String? = targetAddress
        if (AddressUtil.isCrossAddress(targetAddress)) {
            destAddress = ctx.selectFrom(WALLET).where(WALLET.CROSSADDRESS.eq(targetAddress)).fetch().map { it.address }.firstOrNull()
            if (destAddress == null) {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND)
            }
        }
        return WalletServiceRegistry.getService(symbol).send(walletData, destAddress, amount)
    }
}