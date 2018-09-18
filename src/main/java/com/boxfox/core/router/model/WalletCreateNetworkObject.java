package com.boxfox.core.router.model;

import com.google.gson.annotations.SerializedName;

public class WalletCreateNetworkObject extends WalletNetworkObject {

    @SerializedName("walletFileName")
    private String walletFileName;

    @SerializedName("walletData")
    private String walletData;

    public String getWalletFileName() {
        return walletFileName;
    }

    public void setWalletFileName(String walletFileName) {
        this.walletFileName = walletFileName;
    }

    public String getWalletData() {
        return walletData;
    }

    public void setWalletData(String walletData) {
        this.walletData = walletData;
    }
}
