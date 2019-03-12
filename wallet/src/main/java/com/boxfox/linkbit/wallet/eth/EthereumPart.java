package com.boxfox.linkbit.wallet.eth;

import java.io.File;
import org.web3j.protocol.Web3j;

public class EthereumPart {

  protected Web3j web3;
  protected File cachePath;

  public EthereumPart(Web3j web3, File cachePath){
    this.web3 = web3;
    this.cachePath = cachePath;
  }

}
