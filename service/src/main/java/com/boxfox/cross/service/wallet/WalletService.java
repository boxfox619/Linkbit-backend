package com.boxfox.cross.service.wallet;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static com.boxfox.cross.service.wallet.indexing.IndexingMessage.EVENT_SUBJECT;
import static io.one.sys.db.tables.Wallet.WALLET;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.service.wallet.indexing.IndexingMessage;
import com.boxfox.cross.service.wallet.indexing.IndexingService;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
import com.google.gson.Gson;
import io.one.sys.db.tables.daos.AccountDao;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;

public abstract class WalletService {
  private Vertx vertx;
  private IndexingService indexingService;
  private String symbol;

  public WalletService(Vertx vertx, String symbol){
    this.vertx = vertx;
    this.symbol = symbol;
  }

  public WalletService init(){return this;}
  public abstract String getBalance(String address);


  public final WalletCreateResult createWallet(String password, String uid, String symbol, String name, String description){
    WalletCreateResult walletCreateResult = createWallet(password);
    AccountDao dao = new AccountDao(PostgresConfig.create());
    createContext().insertInto(WALLET).values(uid, symbol.toUpperCase(), name, description, walletCreateResult.getAddress(), AddressService.createRandomAddress(dao)).execute();
    return walletCreateResult;
  }

  public abstract WalletCreateResult createWallet(String password);

  public abstract double getPrice(String address);

  public abstract TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

  public abstract Future<List<TransactionStatus>> getTransactionList(String address) throws WalletServiceException;

  public abstract TransactionStatus getTransaction(String transactionHash) throws WalletServiceException;

  public abstract int getTransactionCount(String address) throws WalletServiceException;

  public IndexingService getIndexingService(){ return this.indexingService; }

  public void setIndexingService(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  public void indexingTransactions(String address){
    IndexingMessage msg = new IndexingMessage();
    msg.setSymbol(symbol);
    msg.setAddress(address);
    vertx.eventBus().publish(EVENT_SUBJECT, new Gson().toJson(msg));
  }
}
