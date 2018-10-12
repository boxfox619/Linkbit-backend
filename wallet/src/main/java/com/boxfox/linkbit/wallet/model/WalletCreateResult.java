package com.boxfox.linkbit.wallet.model;


import com.google.gson.JsonObject;

public class WalletCreateResult {
  private boolean result;
  private String address;
  private String walletName;
  private JsonObject walletData;

  public boolean isSuccess() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getWalletName() {
    return walletName;
  }

  public void setWalletName(String name) {
    this.walletName = name;
  }

  public JsonObject getWalletData() {
    return walletData;
  }

  public void setWalletData(JsonObject walletData) {
    this.walletData = walletData;
  }
}
