package com.boxfox.cross.wallet.eth;

import static com.boxfox.cross.common.data.PostgresConfig.create;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.PriceService;
import com.boxfox.cross.service.model.Profile;
import com.boxfox.cross.service.wallet.WalletServiceException;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.boxfox.cross.common.data.Config;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
import com.boxfox.cross.util.FileUtil;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
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
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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
    this.web3 =  Web3j.build(new HttpService("https://mainnet.infura.io/JjSRoXryXbE6HgXJGILz"));
    this.cachePath = new File(Config.getDefaultInstance().getString("cachePath","wallets"));
    if(!cachePath.exists())
      cachePath.mkdirs();
  }

  @Override
  public void init(){
    setIndexingService(new EthIndexingService(web3));
  }

  @Override
  public String getBalance(String address) {
    try {
      String wei = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance().toString();
      BigDecimal bigDecimal = Convert.fromWei(wei, Convert.Unit.ETHER);
      return bigDecimal.toPlainString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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
      indexingTransactions(address);
      File file = FileUtil.encryptFile(jsonFile);
      result.setWalletName(file.getName());
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
  public double getPrice(String address) {
    double price = 0;
    WalletDao dao = new WalletDao(create());
    Wallet wallet = dao.fetchOneByAddress(address);
    if (wallet != null) {
      price = PriceService.getPrice("ETH")* Double.valueOf(getBalance(address));
    }
    return price;
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
  public Future<List<TransactionStatus>> getTransactionList(String address) {
    Future<List<TransactionStatus>> future = Future.future();
    new Thread(() -> {
      List<TransactionStatus> txStatusList = new ArrayList<>();
      TransactionDao transactionDao = new TransactionDao(PostgresConfig.create());
      AccountDao accountDao  = new AccountDao(PostgresConfig.create());
      WalletDao walletDao = new WalletDao(PostgresConfig.create());
      List<io.one.sys.db.tables.pojos.Transaction> transactions = new ArrayList<>();
      transactionDao.fetchByTargetaddress(address).forEach(t -> {transactions.add(t);});
      transactionDao.fetchBySourceaddress(address).forEach(t -> {transactions.add(t);});
      for(io.one.sys.db.tables.pojos.Transaction tx : transactions) {
        TransactionStatus txStatus = new TransactionStatus();
        txStatus.setTransactionHash(tx.getHash());
        txStatus.setAmount(tx.getAmount());
        txStatus.setSourceAddress(tx.getSourceaddress());
        txStatus.setTargetAddress(tx.getTargetaddress());
        txStatus.setTransactionHash(tx.getHash());
        txStatus.setDate(tx.getDatetime());
        txStatus.setVenefit(tx.getTargetaddress().equals(address));
        Wallet sourceWallet = walletDao.fetchOneByAddress((txStatus.isVenefit()) ? tx.getSourceaddress() : tx.getTargetaddress());
        if (sourceWallet != null) {
          Account account = accountDao.fetchOneByUid(sourceWallet.getUid());
          Profile profile = new Profile();
          profile.setUid(account.getUid());
          profile.setEmail(account.getEmail());
          profile.setName(account.getName());
          txStatus.setTargetWallet(com.boxfox.cross.service.model.Wallet.fromDao(sourceWallet));
          txStatus.setTargetProfile(profile);
        }
        txStatusList.add(txStatus);
      }
      future.complete(txStatusList);
    }).start();
    return future;
  }

  @Override
  public TransactionStatus getTransaction(String transactionHash) {
    try {
      Transaction tx = web3.ethGetTransactionByHash(transactionHash).send().getTransaction().get();
      TransactionReceipt receipt = web3.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt().get();
      BigInteger lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber();
      TransactionStatus status = new TransactionStatus();
      BigInteger confirmation = lastBlockNumber.subtract(receipt.getBlockNumber());
      status.setConfirmation(confirmation);
      status.setSourceAddress(receipt.getFrom());
      status.setTargetAddress(receipt.getTo());
      status.setTransactionHash(receipt.getBlockHash());
      status.setAmount(Double.valueOf(Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER).toPlainString()));
      status.setStatus(receipt.getStatus().equals("0x1"));
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
