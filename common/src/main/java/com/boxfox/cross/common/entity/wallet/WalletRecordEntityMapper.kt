package com.boxfox.cross.common.entity.wallet

import com.linkbit.android.entity.WalletModel
import io.one.sys.db.tables.Account
import io.one.sys.db.tables.Wallet
import org.jooq.Record

object WalletRecordEntityMapper{
    fun fromRecord(record: Record): WalletModel {
        return WalletModel().apply {
            this.ownerId = record.get(Wallet.WALLET.UID)
            this.ownerName = record.get(Account.ACCOUNT.NAME)
            this.walletName = record.get(Wallet.WALLET.NAME)
            this.coinSymbol = record.get(Wallet.WALLET.SYMBOL)
            this.description = record.get(Wallet.WALLET.DESCRIPTION)
            this.accountAddress = record.get(Wallet.WALLET.ADDRESS)
            this.linkbitAddress = record.get(Wallet.WALLET.CROSSADDRESS)
        }
    }
}