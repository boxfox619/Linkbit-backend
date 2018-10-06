package com.boxfox.cross.service;

import static com.boxfox.cross.service.AddressService.isCrossAddress;
import static com.boxfox.cross.util.JooqUtil.useContext;
import static io.one.sys.db.tables.Account.ACCOUNT;
import static io.one.sys.db.tables.Majorwallet.MAJORWALLET;
import static io.one.sys.db.tables.Wallet.WALLET;

import com.boxfox.vertx.service.AbstractService;
import com.linkbit.android.entity.WalletModel;
import io.one.sys.db.tables.records.MajorwalletRecord;
import io.one.sys.db.tables.records.WalletRecord;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.jooq.Record;
import org.jooq.Result;

public class WalletDatabaseService extends AbstractService {


    public final void createWallet(String uid, String symbol, String name, String address, String description, boolean open, boolean major, Handler<AsyncResult<WalletModel>> res){
        doAsync(future -> {
            useContext(ctx -> {
                String linkedAddress = AddressService.createRandomAddress(ctx);
                System.out.printf("%s, %s", address, linkedAddress);
                ctx.insertInto(WALLET)
                    .values(uid, symbol.toUpperCase(), name, description, address, linkedAddress, open, major)
                    .execute();
                findByAddress(address, searchRes -> {
                    if (searchRes.failed()) {
                        future.fail("Wallet create fail");
                    } else {
                        future.complete(searchRes.result());
                    }
                    if (!future.isComplete()) {
                        future.fail("");
                    }
                });
            });
        }, res);
    }

    public void findByAddress(String address, Handler<AsyncResult<WalletModel>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                Result<Record> result;
                if (isCrossAddress(address)) {
                    result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.CROSSADDRESS.eq(address)).fetch();
                    if (result.size() == 0) {
                        result = ctx.selectFrom(
                                ACCOUNT
                                        .join(MAJORWALLET).on(MAJORWALLET.UID.eq(ACCOUNT.UID))
                                        .join(WALLET).on(WALLET.ADDRESS.eq(MAJORWALLET.ADDRESS))).where(ACCOUNT.ADDRESS.eq(address))
                                .fetch();
                    }
                } else {
                    result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID)))
                            .where(WALLET.ADDRESS.eq(address)).fetch();
                }
                if (result.size() > 0) {
                    Record record = result.get(0);
                    WalletModel wallet = getWalletFromRecord(record);
                    future.complete(wallet);
                } else {
                    //@TODO un saved wallet data response
                    future.complete();
                }
            });
        }, res);
    }

    public void getMajorWallet(String uid, String symbol, Handler<AsyncResult<WalletModel>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                WalletModel wallet = null;
                Result<Record> records =
                        ctx.selectFrom(MAJORWALLET
                                .join(WALLET).on(MAJORWALLET.ADDRESS.eq(WALLET.ADDRESS)
                                        .and(MAJORWALLET.SYMBOL.eq(WALLET.SYMBOL)))
                                .join(ACCOUNT).on(WALLET.UID.eq(ACCOUNT.UID)))
                                .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol)))
                                .fetch();
                if (records.size() > 0) {
                    Record record = records.get(0);
                    wallet = getWalletFromRecord(record);
                }
                future.complete(wallet);
            });
        }, res);
    }

    public void setMajorWallet(String uid, String symbol, String address,
                               Handler<AsyncResult<Void>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                Result<MajorwalletRecord> records = ctx.selectFrom(MAJORWALLET)
                        .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).fetch();
                if (records.size() > 0) {
                    ctx.update(MAJORWALLET).set(MAJORWALLET.ADDRESS, address)
                            .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).execute();
                } else {
                    ctx.insertInto(MAJORWALLET).values(uid, symbol, address).execute();
                }
            });
            future.complete();
        }, res);
    }

    public void updateWallet(String uid, String address, String name, String description, boolean major, boolean open, Handler<AsyncResult<Void>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                int updatedRows = ctx.update(WALLET)
                        .set(WALLET.NAME, name)
                        .set(WALLET.DESCRIPTION, description)
                        .set(WALLET.MAJOR, major)
                        .set(WALLET.PUBLISH, open)
                        .where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid)))
                        .execute();
                if (updatedRows > 0) {
                    future.complete();
                } else {
                    future.fail("Wallet update fail");
                }
            });
        }, res);
    }

    public void deleteWallet(String uid, String address, Handler<AsyncResult<Void>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                int deletedRows = ctx.delete(WALLET).where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid))).execute();
                if (deletedRows > 0) {
                    future.complete();
                } else {
                    future.fail("Wallet update fail");
                }
            });
        }, res);
    }

    public void checkOwner(String uid, String address, Handler<AsyncResult<Void>> res) {
        doAsync(future -> {
            useContext(ctx -> {
                Result<WalletRecord> result = ctx.selectFrom(WALLET).where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid))).fetch();
                if (result.size() == 1) {
                    future.complete();
                }else{
                    future.fail("Not owner");
                }
            });
        }, res);
    }

    public static WalletModel getWalletFromRecord(Record record) {
        WalletModel wallet = new WalletModel();
        wallet.setOwnerId(record.getValue(WALLET.UID));
        wallet.setOwnerName(record.getValue(ACCOUNT.NAME));
        wallet.setWalletName(record.getValue(WALLET.NAME));
        wallet.setCoinSymbol(record.getValue(WALLET.SYMBOL));
        wallet.setDescription(record.getValue(WALLET.DESCRIPTION));
        wallet.setAccountAddress(record.get(WALLET.ADDRESS));
        wallet.setLinkbitAddress(record.get(WALLET.CROSSADDRESS));
        return wallet;
    }
}
