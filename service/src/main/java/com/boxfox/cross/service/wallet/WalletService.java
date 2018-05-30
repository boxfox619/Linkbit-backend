package com.boxfox.cross.service.wallet;

import com.boxfox.cross.service.wallet.indexing.IndexingService;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import io.vertx.core.json.JsonObject;
import java.util.List;

public abstract class WalletService {

  public void init(){}
  public abstract String getBalance(String address);
  public abstract JsonObject createWallet(String password);
  public abstract TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

  public abstract List<TransactionStatus> getTransactionList(String address) throws WalletException;

  public abstract TransactionStatus getTransaction(String transactionHash) throws WalletException;

  public abstract int getTransactionCount(String address) throws WalletException;

  public IndexingService getIndexingService(){ return null; }
}
