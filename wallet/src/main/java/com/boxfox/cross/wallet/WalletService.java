package com.boxfox.cross.wallet;

import static com.boxfox.cross.wallet.indexing.IndexingMessage.EVENT_SUBJECT;
import static io.one.sys.db.tables.Account.ACCOUNT;
import static io.one.sys.db.tables.Wallet.WALLET;

import com.boxfox.cross.common.vertx.service.AbstractService;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.wallet.indexing.IndexingMessage;
import com.boxfox.cross.wallet.indexing.IndexingService;
import com.boxfox.cross.wallet.model.TransactionResult;
import com.boxfox.cross.wallet.model.WalletCreateResult;
import com.google.gson.Gson;
import com.linkbit.android.entity.TransactionModel;
import com.linkbit.android.entity.WalletModel;
import io.one.sys.db.tables.pojos.Wallet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jooq.Record;

import java.util.List;

public abstract class WalletService extends AbstractService{
  private IndexingService indexingService;
  private String symbol;

  public WalletService(Vertx vertx, String symbol){
    super(vertx);
    this.symbol = symbol;
  }

  public abstract double getBalance(String originalAddress);


  public final void createWallet( String uid, String name, String password, String description, Handler<AsyncResult<WalletCreateResult>> res){
    doAsync(future -> {
      WalletCreateResult walletCreateResult = createWallet(password);
      if(walletCreateResult.isSuccess()) {
        useContext(ctx -> {
          ctx.insertInto(WALLET).values(uid, symbol.toUpperCase(), name, description, walletCreateResult.getAddress(), AddressService.createRandomAddress(ctx)).execute();
        });
        future.complete(walletCreateResult);
      }else{
        future.fail("Wallet create fail");
      }
    }, res);
  }

  public abstract WalletCreateResult createWallet(String password);

  public abstract TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

  public abstract Future<List<TransactionModel>> getTransactionList(String address) throws WalletServiceException;

  public abstract TransactionModel getTransaction(String transactionHash) throws WalletServiceException;

  public abstract int getTransactionCount(String address) throws WalletServiceException;

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

  public static WalletModel getWalletFromRecord(Record record) {
    WalletModel wallet = new WalletModel();
    wallet.setOwnerId(record.getValue(WALLET.UID));
    wallet.setOwnerName(record.getValue(ACCOUNT.NAME));
    wallet.setWalletName(record.getValue(WALLET.NAME));
    wallet.setCoinSymbol(record.getValue(WALLET.SYMBOL));
    wallet.setDescription(record.getValue(WALLET.DESCRIPTION));
    wallet.setOriginalAddress(record.get(WALLET.ADDRESS));
    wallet.setLinkbitAddress(record.get(WALLET.CROSSADDRESS));
    return wallet;
  }

  public static WalletModel getWalletFromDao(Wallet wallet){
    WalletModel newWallet = new WalletModel();
    newWallet.setOwnerId(wallet.getUid());
    //newWallet.setOwnerName(wallet.());
    newWallet.setWalletName(wallet.getName());
    newWallet.setCoinSymbol(wallet.getSymbol());
    newWallet.setDescription(wallet.getDescription());
    newWallet.setOriginalAddress(wallet.getAddress());
    newWallet.setLinkbitAddress(wallet.getCrossaddress());
    return newWallet;
  }
}
