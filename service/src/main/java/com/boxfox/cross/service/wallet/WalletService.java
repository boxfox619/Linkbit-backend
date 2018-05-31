package com.boxfox.cross.service.wallet;

import com.boxfox.cross.service.wallet.indexing.IndexingMessage;
import com.boxfox.cross.service.wallet.indexing.IndexingService;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
import com.google.gson.Gson;
import io.one.sys.db.tables.Wallet;
import io.one.sys.db.tables.records.WalletRecord;
import io.vertx.core.Vertx;
import java.util.List;

public abstract class WalletService {
  private IndexingService indexingService;

  public void init(){}
  public abstract String getBalance(String address);


  public final WalletCreateResult createWallet(String password, String uid, String symbol, String name, String description){
    WalletCreateResult walletCreateResult = createWallet(password);
    WalletRecord walletRecord = Wallet.WALLET.newRecord();
    walletRecord.setUid(uid);
    walletRecord.setName(name);
    walletRecord.setDescription(description);
    walletRecord.setSymbol(symbol);
    walletRecord.setAddress(walletCreateResult.getAddress());
    walletRecord.store();
    return walletCreateResult;
  }

  public abstract WalletCreateResult createWallet(String password);

  public abstract TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

  public abstract List<TransactionStatus> getTransactionList(String address) throws WalletException;

  public abstract TransactionStatus getTransaction(String transactionHash) throws WalletException;

  public abstract int getTransactionCount(String address) throws WalletException;

  public IndexingService getIndexingService(){ return this.indexingService; }

  public void setIndexingService(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  public void indexingTransactions(String address){
    IndexingMessage msg = new IndexingMessage();
    msg.setSymbol("eth");
    msg.setAddress(address);
    Vertx.vertx().eventBus().send(IndexingMessage.EVENT_SUBJECT, new Gson().toJson(msg));
  }
}
