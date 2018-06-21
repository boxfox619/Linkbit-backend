package com.boxfox.cross.wallet.model;

public class TransactionResult {
  private boolean status = false;
  private String transactionHash;

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }
}
