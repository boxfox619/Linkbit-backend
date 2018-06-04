package com.boxfox.cross.service;

import com.boxfox.cross.service.model.Wallet;
import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.records.MajorwalletRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import static io.one.sys.db.tables.Account.ACCOUNT;
import static io.one.sys.db.tables.Majorwallet.MAJORWALLET;
import static io.one.sys.db.tables.Wallet.WALLET;
import static com.boxfox.cross.common.data.PostgresConfig.createContext;

public class AddressService {
    private static final String ADDRESS_REGEX = "cross-[0-9]{0,4}-[0-9]{0,4}";

    public Wallet findByAddress(String symbol, String address) {
        Wallet wallet = null;
        DSLContext ctx = createContext();
        Result<Record> result;
        if(isMathCrossAddress(address)){
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.SYMBOL.eq(symbol).and(WALLET.CROSSADDRESS.eq(address))).fetch();
            if(result.size()==0){
                result = ctx.selectFrom(ACCOUNT.join(MAJORWALLET).on(MAJORWALLET.UID.eq(ACCOUNT.UID)).join(WALLET).on(WALLET.ADDRESS.eq(MAJORWALLET.ADDRESS))).where(ACCOUNT.ADDRESS.eq(address)).fetch();
            }
        } else {
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.ADDRESS.eq(address).and(WALLET.SYMBOL.eq(symbol))).fetch();
        }
        if(result.size()>0){
            Record record = result.get(0);
            wallet = Wallet.fromRecord(record);
            wallet.setOwner(record.get(ACCOUNT.EMAIL));
            wallet.setOwnerName(record.getValue(ACCOUNT.NAME));
        }
        ctx.close();
        return wallet;
    }

    public Wallet getMajorWallet(String uid, String symbol){
        Wallet wallet = null;
        DSLContext ctx = createContext();
        Result<Record> records = ctx.selectFrom(MAJORWALLET.join(WALLET).on(MAJORWALLET.ADDRESS.eq(WALLET.ADDRESS).and(MAJORWALLET.SYMBOL.eq(WALLET.SYMBOL)))).where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).fetch();
        if(records.size()>0){
            Record record = records.get(0);
            wallet = Wallet.fromRecord(record);
        }
        ctx.close();
        return wallet;
    }

    public void setMajorWallet(String uid, String symbol, String address){
        DSLContext ctx = createContext();
        Result<MajorwalletRecord> records = ctx.selectFrom(MAJORWALLET).where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).fetch();
        if(records.size()>0){
            MajorwalletRecord record = records.get(0);
            record.setAddress(address);
            record.store();
        }else{
            ctx.insertInto(MAJORWALLET).values(uid,symbol,address).execute();
        }
        ctx.close();
    }

    public static boolean isMathCrossAddress(String address) {
        return address.matches(ADDRESS_REGEX);
    }

    public static String createRandomAddress(AccountDao accountDao) {
        String address;
        do {
            int firstNum = (int) (Math.random() * 9999 + 1);
            int secondNum = (int) (Math.random() * 999999 + 1);
            address = String.format("cross-%04d-%04d", firstNum, secondNum);
        } while (accountDao.fetchByAddress(address).size() > 0);
        return address;
    }
}
