package com.boxfox.linkbit.wallet.eth;

import io.vertx.core.Vertx;
import java.io.File;
import org.web3j.protocol.Web3j;

public class EthereumPart {

  protected Vertx vertx;
  protected Web3j web3;
  protected File cachePath;

  public EthereumPart(Vertx vertx, Web3j web3, File cachePath){
    this.vertx = vertx;
    this.web3 = web3;
    this.cachePath = cachePath;
  }

}
