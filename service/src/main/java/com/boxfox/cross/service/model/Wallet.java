package com.boxfox.cross.service.model;

import org.jooq.Record;

import static io.one.sys.db.tables.Wallet.WALLET;

public class Wallet {
    private String uid;
    private String ownerEmail;
    private String ownerName;
    private String name;
    private String symbol;
    private String description;
    private String originalAddress;
    private String crossAddress;
    private String balance;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalAddress() {
        return originalAddress;
    }

    public void setOriginalAddress(String originalAddress) {
        this.originalAddress = originalAddress;
    }

    public String getCrossAddress() {
        return crossAddress;
    }

    public void setCrossAddress(String crossAddress) {
        this.crossAddress = crossAddress;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public static Wallet fromRecord(Record record){
        Wallet wallet = new Wallet();
        wallet.setUid(record.getValue(WALLET.UID));
        wallet.setName(record.getValue(WALLET.NAME));
        wallet.setSymbol(record.getValue(WALLET.SYMBOL));
        wallet.setDescription(record.getValue(WALLET.DESCRIPTION));
        wallet.setOriginalAddress(record.get(WALLET.ADDRESS));
        wallet.setCrossAddress(record.get(WALLET.CROSSADDRESS));
        return wallet;
    }

    public static Wallet fromDao(io.one.sys.db.tables.pojos.Wallet wallet){
        Wallet newWallet = new Wallet();
        newWallet.setUid(wallet.getUid());
        newWallet.setName(wallet.getName());
        newWallet.setSymbol(wallet.getSymbol());
        newWallet.setDescription(wallet.getDescription());
        newWallet.setOriginalAddress(wallet.getAddress());
        newWallet.setCrossAddress(wallet.getCrossaddress());
        return newWallet;
    }
}
