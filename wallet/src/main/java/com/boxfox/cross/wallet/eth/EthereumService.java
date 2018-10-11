package com.boxfox.cross.wallet.eth;

import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.part.BalancePart;
import com.boxfox.cross.wallet.part.CreateWalletPart;
import com.boxfox.cross.wallet.part.TransactionPart;
import com.boxfox.vertx.data.Config;
import java.io.File;
import io.vertx.core.Vertx;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class EthereumService extends WalletService {
  private EthereumBalancePart balancePart;
  private EthereumCreateWalletPart createWalletPart;
  private EthereumTransactionPart transactionPart;
  private EthIndexingService indexingService;

  public EthereumService(Vertx vertx){
    super(vertx,"ETH");
    Web3j web3 =  Web3j.build(new HttpService("https://mainnet.infura.io/v3/326b0d7561824e0b8c4ee1f30e257019"));
    File cachePath = new File(Config.getDefaultInstance().getString("cachePath","wallets"));
    if(!cachePath.exists())
      cachePath.mkdirs();
    this.balancePart = new EthereumBalancePart(vertx, web3, cachePath);
    this.createWalletPart = new EthereumCreateWalletPart(vertx, web3, cachePath);
    this.transactionPart = new EthereumTransactionPart(vertx, web3, cachePath);
    this.indexingService = new EthIndexingService(web3, vertx);
  }

  @Override
  public void init(){
    setIndexingService(this.indexingService);
  }

  @Override
  public BalancePart getBalancePart() {
    return this.balancePart;
  }

  @Override
  public CreateWalletPart getCreateWalletPart() {
    return this.createWalletPart;
  }

  @Override
  public TransactionPart getTransactionPart() {
    return this.transactionPart;
  }

}
