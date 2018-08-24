package com.boxfox.cross.service;

import static io.one.sys.db.tables.Account.ACCOUNT;
import static io.one.sys.db.tables.Majorwallet.MAJORWALLET;
import static io.one.sys.db.tables.Wallet.WALLET;

import com.boxfox.cross.common.vertx.service.AbstractService;
import com.linkbit.android.entity.WalletModel;
import io.one.sys.db.tables.records.MajorwalletRecord;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

public class AddressService extends AbstractService {

  private static final String ADDRESS_REGEX = "cross-[0-9]{0,4}-[0-9]{0,4}";

  public void findByAddress(String address, Handler<AsyncResult<WalletModel>> res) {
    doAsync(future -> {
      useContext(ctx -> {
        Result<Record> result;
        if (isCrossAddress(address)) {
          result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID)))
              .where(WALLET.CROSSADDRESS.eq(address)).fetch();
          if (result.size() == 0) {
            result = ctx.selectFrom(
                ACCOUNT.join(MAJORWALLET).on(MAJORWALLET.UID.eq(ACCOUNT.UID)).join(WALLET)
                    .on(WALLET.ADDRESS.eq(MAJORWALLET.ADDRESS))).where(ACCOUNT.ADDRESS.eq(address))
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

  public static boolean isCrossAddress(String address) {
    return address.matches(ADDRESS_REGEX);
  }

  public static String createRandomAddress(DSLContext ctx) {
    String address;
    do {
      int firstNum = (int) (Math.random() * 9999 + 1);
      int secondNum = (int) (Math.random() * 999999 + 1);
      address = String.format("cross-%04d-%04d", firstNum, secondNum);
    } while (isValidAddress(ctx, address));
    return address;
  }

  public static boolean isValidAddress(DSLContext ctx, String address) {
    return (ctx.selectFrom(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetch().size() > 0 ||
        ctx.selectFrom(WALLET).where(WALLET.CROSSADDRESS.eq(address)).fetch().size() > 0);
  }


  public static WalletModel getWalletFromRecord(Record record) {
    WalletModel wallet = new WalletModel();
    wallet.setOwnerId(record.getValue(WALLET.UID));
    wallet.setOwnerName(record.getValue(ACCOUNT.NAME));
    wallet.setWalletName(record.getValue(WALLET.NAME));
    wallet.setCoinSymbol(record.getValue(WALLET.SYMBOL));
    wallet.setDescription(record.getValue(WALLET.DESCRIPTION));
    wallet.setOriginalAddress(record.get(WALLET.ADDRESS));
    wallet.setLinkbitAddress(record.get(WALLET.CROSSADDRESS));
    return wallet;
  }
}
