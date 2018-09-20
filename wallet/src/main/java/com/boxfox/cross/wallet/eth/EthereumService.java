package com.boxfox.cross.wallet.eth;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceException;
import com.boxfox.cross.wallet.model.TransactionResult;
import com.boxfox.cross.wallet.model.WalletCreateResult;
import com.boxfox.vertx.data.Config;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.linkbit.android.entity.TransactionModel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.one.sys.db.tables.daos.AccountDao;
import io.one.sys.db.tables.daos.TransactionDao;
import io.one.sys.db.tables.daos.WalletDao;
import io.one.sys.db.tables.pojos.Account;
import io.one.sys.db.tables.pojos.Wallet;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

public class EthereumService extends WalletService {

  static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
  static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
  private Web3j web3;
  private File cachePath;

  public EthereumService(Vertx vertx){
    super(vertx,"ETH");
    this.web3 =  Web3j.build(new HttpService("https://mainnet.infura.io/v3/326b0d7561824e0b8c4ee1f30e257019"));
    this.cachePath = new File(Config.getDefaultInstance().getString("cachePath","wallets"));
    if(!cachePath.exists())
      cachePath.mkdirs();
  }

  @Override
  public void init(){
    setIndexingService(new EthIndexingService(web3, getVertx()));
  }

  @Override
  public double getBalance(String address) {
    try {
      EthGetBalance response = web3.ethGetBalance(address.replaceAll(" ",""), DefaultBlockParameterName.LATEST).send();
      String wei = response.getBalance().toString();
      BigDecimal bigDecimal = Convert.fromWei(wei, Convert.Unit.ETHER);
      return bigDecimal.doubleValue();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public WalletCreateResult createWallet(String password) {
    WalletCreateResult result = new WalletCreateResult();
    try {
      String walletFileName = WalletUtils.generateFullNewWalletFile(password, cachePath);
      File jsonFile = new File(cachePath.getPath() + File.separator + walletFileName);
      String walletJson = Files.toString(jsonFile, Charset.defaultCharset());
      JsonObject walletJsonObj = (JsonObject) new JsonParser().parse(walletJson);
      String address = "0x"+walletJsonObj.get("address").getAsString();
      result.setResult(true);
      result.setAddress(address);
      result.setWalletName(walletFileName);
      result.setWalletData(walletJsonObj);
      indexingTransactions(address);
      jsonFile.delete();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    } catch (CipherException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount) {
    File tmpWallet = new File(cachePath.getPath() + File.separator + walletFileName);
    TransactionResult result = new TransactionResult();
    try {
      Files.write(walletJsonFile, tmpWallet, Charset.forName("UTF-8"));
      Credentials credentials1 = WalletUtils.loadCredentials(password, tmpWallet.getPath());
      EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials1.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

      BigInteger nonce = ethGetTransactionCount.getTransactionCount();

      BigInteger amountEth = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
      RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, GAS_PRICE, GAS_LIMIT, targetAddress, amountEth);

      byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials1);
      String hexValue = Numeric.toHexString(signedMessage);

      EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
      String transactionHash = ethSendTransaction.getTransactionHash();
      result.setStatus(true);
      result.setTransactionHash(transactionHash);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (CipherException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public Future<List<TransactionModel>> getTransactionList(String address) {
    Future<List<TransactionModel>> future = Future.future();
    new Thread(() -> {
      List<TransactionModel> txStatusList = new ArrayList<>();
      TransactionDao transactionDao = new TransactionDao(PostgresConfig.create(),getVertx());
      AccountDao accountDao  = new AccountDao(PostgresConfig.create(),getVertx());
      WalletDao walletDao = new WalletDao(PostgresConfig.create(),getVertx());
      List<io.one.sys.db.tables.pojos.Transaction> transactions = new ArrayList<>();
      transactionDao.findManyByTargetaddress(Arrays.asList(address)).result().forEach(t -> {transactions.add(t);});
      transactionDao.findManyBySourceaddress(Arrays.asList(address)).result().forEach(t -> {transactions.add(t);});
      for(io.one.sys.db.tables.pojos.Transaction tx : transactions) {
        TransactionModel txStatus = new TransactionModel();
        txStatus.setTransactionHash(tx.getHash());
        txStatus.setAmount(tx.getAmount());
        txStatus.setSourceAddress(tx.getSourceaddress());
        txStatus.setTargetAddress(tx.getTargetaddress());
        txStatus.setTransactionHash(tx.getHash());
        txStatus.setDate(tx.getDatetime());
        Wallet wallet = walletDao.findOneById(tx.getTargetaddress()).result();
        if (wallet != null) {
          txStatus.setTargetProfile(wallet.getName());
        } else {
          txStatus.setTargetProfile("Unknown");
        }
        //txStatus.setStatus();
        //txStatus.setBlockNumber();
        //txStatus.setConfirmation();
        //@TODO more add tx status
        txStatusList.add(txStatus);
      }
      future.complete(txStatusList);
    }).start();
    return future;
  }

  @Override
  public TransactionModel getTransaction(String transactionHash) {
    try {
      Transaction tx = web3.ethGetTransactionByHash(transactionHash).send().getTransaction().get();
      TransactionReceipt receipt = web3.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt().get();
      BigInteger lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber();
      TransactionModel status = new TransactionModel();
      BigInteger confirmation = lastBlockNumber.subtract(receipt.getBlockNumber());
      status.setConfirmation(confirmation.intValue());
      status.setSourceAddress(receipt.getFrom());
      status.setTargetAddress(receipt.getTo());
      status.setTransactionHash(receipt.getBlockHash());
      status.setAmount(Double.valueOf(Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER).toPlainString()));
      status.setStatus(receipt.getStatus().equals("0x1"));
      status.setBlockNumber(receipt.getBlockNumber().intValue());
      //txStatus.setDate(tx.getDatetime());
      //txStatus.setTargetProfile();
      //txStatus.setBlockNumber();
      //@TODO more add tx status

      return status;
    } catch (IOException e) {
      throw new WalletServiceException("Can not lookup transaction status");
    }
  }

  @Override
  public int getTransactionCount(String address) {
    try {
      return web3.ethGetTransactionCount(address,DefaultBlockParameterName.LATEST).send().getTransactionCount().intValue();
    } catch (IOException e) {
      throw new WalletServiceException("Can not get transaction count");
    }
  }
}
