package com.boxfox.cross.service.wallet.model;

import com.google.gson.JsonObject;

public class WalletCreateResult {
  private boolean result;
  private String address;
  private String name;
  private JsonObject wallet;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JsonObject getWallet() {
    return wallet;
  }

  public void setWallet(JsonObject wallet) {
    this.wallet = wallet;
  }
}
