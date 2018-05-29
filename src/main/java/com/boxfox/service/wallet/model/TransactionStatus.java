package com.boxfox.service.wallet.model;

import java.math.BigInteger;

public class TransactionStatus {
  private String transactionHash;
  private String sourceAddress;
  private String targetAddress;
  private boolean status;
  private BigInteger amount;
  private BigInteger blockNumber;
  private BigInteger confirmation;

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  public String getSourceAddress() {
    return sourceAddress;
  }

  public void setSourceAddress(String sourceAddress) {
    this.sourceAddress = sourceAddress;
  }

  public String getTargetAddress() {
    return targetAddress;
  }

  public void setTargetAddress(String targetAddress) {
    this.targetAddress = targetAddress;
  }

  public BigInteger getAmount() {
    return amount;
  }

  public void setAmount(BigInteger amount) {
    this.amount = amount;
  }

  public BigInteger getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(BigInteger blockNumber) {
    this.blockNumber = blockNumber;
  }

  public BigInteger getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(BigInteger confirmation) {
    this.confirmation = confirmation;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public boolean getStatus() {
    return status;
  }
}
