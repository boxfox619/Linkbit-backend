package com.boxfox.core.router.model;

import com.google.gson.annotations.SerializedName;

public class WalletNetworkObject {

    @SerializedName("ownerId")
    private String ownerId;
    @SerializedName("ownerName")
    private String ownerName;
    @SerializedName("walletName")
    private String walletName;
    @SerializedName("coinSymbol")
    private String coinSymbol;
    @SerializedName("description")
    private String description;
    @SerializedName("accountAddress")
    private String accountAddress;
    @SerializedName("linkbitAddress")
    private String linkbitAddress;
    @SerializedName("balance")
    private double balance = 0.0;


    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getCoinSymbol() {
        return coinSymbol;
    }

    public void setCoinSymbol(String coinSymbol) {
        this.coinSymbol = coinSymbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public String getLinkbitAddress() {
        return linkbitAddress;
    }

    public void setLinkbitAddress(String linkbitAddress) {
        this.linkbitAddress = linkbitAddress;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
