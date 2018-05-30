package com.boxfox.cross.wallet.eth;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;

import com.boxfox.cross.service.wallet.indexing.IndexingService;
import io.vertx.core.Vertx;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Transaction;
import static io.one.sys.db.tables.Transaction.TRANSACTION;

public class EthIndexingService implements IndexingService {
  private Web3j web3;

  protected EthIndexingService(Web3j web3){
    this.web3 = web3;
  }

  @Override
  public void indexing(Vertx vertx, String address) {
    try {
      BigInteger lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber();
      int txCount = web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().getTransactionCount().intValue();
      int balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance().intValue();
      int idx = lastBlockNumber.intValue();
      for (; idx >= 0 && (txCount > 0 || balance > 0); --idx) {
        Block block = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(idx)), false).send().getBlock();
        List<TransactionResult> transactions =  block.getTransactions();
        if (block!=null && block.getTransactions()!=null) {
          for(EthBlock.TransactionResult txResult : transactions){
            Transaction tx = (Transaction) txResult.get();
            if (address.equals(tx.getFrom())) {
              if (!tx.getFrom().equals(tx.getTo()))
                balance += tx.getValue().intValue();
              txCount-=1;
              indexingTransaction(tx);
            }
            if (address.equals(tx.getTo())) {
              if (!tx.getFrom().equals(tx.getTo()))
                balance-= tx.getValue().intValue();
              indexingTransaction(tx);
            }
          }
        }
      }
    } catch (IOException e) {

    }
  }

  private void indexingTransaction(Transaction tx) {
    String from = tx.getFrom();
    String to = tx.getTo();
    String txHash = tx.getHash();
    BigInteger amount = tx.getValue();
    createContext().insertInto(TRANSACTION).values(from, to, amount, txHash).execute();
  }
}
