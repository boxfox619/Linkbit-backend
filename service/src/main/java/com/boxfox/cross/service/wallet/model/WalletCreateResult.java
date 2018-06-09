package com.boxfox.cross.service.wallet.model;


public class WalletCreateResult {
  private boolean result;
  private String address;
  private transient  String walletName;

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
}
