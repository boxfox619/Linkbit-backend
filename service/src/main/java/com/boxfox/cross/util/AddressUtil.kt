package com.boxfox.cross.util

import io.one.sys.db.tables.Account
import io.one.sys.db.tables.Wallet
import io.one.sys.db.tables.records.AccountRecord
import io.one.sys.db.tables.records.WalletRecord
import org.jooq.DSLContext

object AddressUtil {

    private val ADDRESS_REGEX = "cross-[0-9]{0,4}-[0-9]{0,4}"

    fun isCrossAddress(address: String): Boolean {
        return address.matches(ADDRESS_REGEX.toRegex())
    }

    fun createRandomAddress(ctx: DSLContext): String {
        var address: String
        do {
            val firstNum = (Math.random() * 9999 + 1).toInt()
            val secondNum = (Math.random() * 999999 + 1).toInt()
            address = String.format("linkbit-%04d-%06d", firstNum, secondNum)
        } while (isValidAddress(ctx, address))
        return address
    }

    fun isValidAddress(ctx: DSLContext, address: String): Boolean {
        return ctx.selectFrom<AccountRecord>(Account.ACCOUNT)
                .where(Account.ACCOUNT.ADDRESS.eq(address)).fetch().size > 0
                || ctx.selectFrom<WalletRecord>(Wallet.WALLET)
                .where(Wallet.WALLET.CROSSADDRESS.eq(address)).fetch().size > 0
    }
}