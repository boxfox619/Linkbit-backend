package com.boxfox.cross.service.wallet.indexing;

public class IndexingMessage {
  public static transient final String EVENT_SUBJECT = "wallet.indexing";
  private String symbol;
  private String address;

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
