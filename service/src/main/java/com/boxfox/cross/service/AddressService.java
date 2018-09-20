package com.boxfox.cross.service;

import static io.one.sys.db.tables.Account.ACCOUNT;
import static io.one.sys.db.tables.Wallet.WALLET;

import com.boxfox.vertx.vertx.service.AbstractService;
import org.jooq.DSLContext;

public class AddressService extends AbstractService {

  private static final String ADDRESS_REGEX = "cross-[0-9]{0,4}-[0-9]{0,4}";


  public static boolean isCrossAddress(String address) {
    return address.matches(ADDRESS_REGEX);
  }

  public static String createRandomAddress(DSLContext ctx) {
    String address;
    do {
      int firstNum = (int) (Math.random() * 9999 + 1);
      int secondNum = (int) (Math.random() * 999999 + 1);
      address = String.format("linkbit-%04d-%06d", firstNum, secondNum);
    } while (isValidAddress(ctx, address));
    return address;
  }

  public static boolean isValidAddress(DSLContext ctx, String address) {
    return (ctx.selectFrom(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetch().size() > 0 ||
        ctx.selectFrom(WALLET).where(WALLET.CROSSADDRESS.eq(address)).fetch().size() > 0);
  }
}
