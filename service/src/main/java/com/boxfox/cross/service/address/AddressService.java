package com.boxfox.cross.service.address;

import io.one.sys.db.tables.daos.AccountDao;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import static io.one.sys.db.tables.Account.ACCOUNT;
import static io.one.sys.db.tables.Wallet.WALLET;
import static com.boxfox.cross.common.data.PostgresConfig.createContext;

public class AddressService {
    private static final String ADDRESS_REGEX = "[0-9]{4}-[0-9]{6}-[0-9]{2}";

    public Wallet findByAddress(String symbol, String address) {
        Wallet wallet = null;
        DSLContext ctx = createContext();
        Result<Record> result;
        if(isMathCrossAddress(address)){
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.SYMBOL.eq(symbol).and(WALLET.CROSSADDRESS.eq(address))).fetch();
        }else{
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.ADDRESS.eq(address).and(WALLET.SYMBOL.eq(symbol))).fetch();
        }
        if(result.size()>0){
            Record record = result.get(0);
            wallet = new Wallet();
            wallet.setUid(record.getValue(WALLET.UID));
            wallet.setName(record.getValue(WALLET.NAME));
            wallet.setSymbol(symbol);
            wallet.setDescription(record.getValue(WALLET.DESCRIPTION));
            wallet.setOriginalAddress(record.get(WALLET.ADDRESS));
            wallet.setCrossAddress(record.get(ACCOUNT.ADDRESS));
            wallet.setOwner(record.get(ACCOUNT.EMAIL));
        }
        return wallet;
    }

    public static boolean isMathCrossAddress(String address) {
        return address.matches(ADDRESS_REGEX);
    }

    public static String createRandomAddress(AccountDao accountDao) {
        String address;
        do {
            int firstNum = (int) (Math.random() * 9999 + 1);
            int secondNum = (int) (Math.random() * 999999 + 1);
            int lastNum = (int) (Math.random() * 99 + 1);
            address = String.format("%04d-%06d-$02d", firstNum, secondNum, lastNum);
        } while (accountDao.fetchByAddress(address).size() > 0);
        return address;
    }
}
