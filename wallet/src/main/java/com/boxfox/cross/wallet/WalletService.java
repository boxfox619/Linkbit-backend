package com.boxfox.cross.wallet;

import static com.boxfox.cross.wallet.indexing.IndexingMessage.EVENT_SUBJECT;

import com.boxfox.cross.wallet.indexing.IndexingMessage;
import com.boxfox.cross.wallet.indexing.IndexingService;
import com.boxfox.cross.wallet.model.TransactionResult;
import com.boxfox.cross.wallet.model.WalletCreateResult;
import com.boxfox.cross.wallet.part.BalancePart;
import com.boxfox.cross.wallet.part.CreateWalletPart;
import com.boxfox.cross.wallet.part.TransactionPart;
import com.boxfox.vertx.service.AbstractService;
import com.google.gson.Gson;
import com.linkbit.android.entity.TransactionModel;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.util.List;

public abstract class WalletService extends AbstractService implements BalancePart, CreateWalletPart, TransactionPart {
  private IndexingService indexingService;
  protected String symbol;

  public WalletService(Vertx vertx, String symbol){
    super(vertx);
    this.symbol = symbol;
  }

  @Override
  public double getBalance(String originalAddress) {
    return getBalancePart().getBalance(originalAddress);
  }

  @Override
  public WalletCreateResult createWallet(String password) {
    return getCreateWalletPart().createWallet(password);
  }

  @Override
  public TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount) {
    return getTransactionPart().send(walletFileName, walletJsonFile, password, targetAddress, amount);
  }

  @Override
  public Future<List<TransactionModel>> getTransactionList(String address) throws WalletServiceException {
    this.indexingTransactions(address);
    return getTransactionPart().getTransactionList(address);
  }

  @Override
  public TransactionModel getTransaction(String transactionHash) throws WalletServiceException {
    return getTransactionPart().getTransaction(transactionHash);
  }

  @Override
  public int getTransactionCount(String address) throws WalletServiceException {
    return getTransactionPart().getTransactionCount(address);
  }

  public BalancePart getBalancePart() {
    return address -> 0;
  }

  public CreateWalletPart getCreateWalletPart() {
    return password -> null;
  }

  public TransactionPart getTransactionPart() {
    return null;
  }

  public IndexingService getIndexingService(){ return this.indexingService; }

  public void setIndexingService(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  public void indexingTransactions(String address){
    IndexingMessage msg = new IndexingMessage();
    msg.setSymbol(symbol);
    msg.setAddress(address);
    getVertx().eventBus().publish(EVENT_SUBJECT, new Gson().toJson(msg));
  }
}
