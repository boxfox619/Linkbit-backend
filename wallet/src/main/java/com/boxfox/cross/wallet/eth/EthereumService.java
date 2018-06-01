package com.boxfox.cross.wallet.eth;

import static com.boxfox.cross.common.data.PostgresConfig.create;
import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.tables.Wallet.WALLET;
import static io.one.sys.db.tables.Transaction.TRANSACTION;

import com.boxfox.cross.service.wallet.WalletServiceException;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.boxfox.cross.common.data.Config;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
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

import io.one.sys.db.tables.daos.WalletDao;
import io.one.sys.db.tables.pojos.Wallet;
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

  public EthereumService(){
    this.web3 =  Web3j.build(new HttpService("https://mainnet.infura.io/JjSRoXryXbE6HgXJGILz"));
    this.cachePath = new File(Config.getDefaultInstance().getString("cachePath"));
    if(!cachePath.exists())
      cachePath.mkdirs();
  }

  public void init(){
    setIndexingService(new EthIndexingService(web3));
    web3.transactionObservable().subscribe(tx -> {
      String from = tx.getFrom();
      String to = tx.getTo();
      if(createContext().selectFrom(WALLET).where(WALLET.ADDRESS.equal(from).or(WALLET.ADDRESS.equal(to))).fetch().size()>0){
        createContext().insertInto(TRANSACTION).values(from, to, tx.getHash());
      }
    });
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
      jsonFile.delete();
      JsonObject walletJsonObj = (JsonObject) new JsonParser().parse(walletJson);
      String address = "0x"+walletJsonObj.get("address").getAsString();
      result.setResult(true);
      result.setName(walletFileName);
      result.setWallet(walletJsonObj);
      result.setAddress(address);
      indexingTransactions(address);
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
      price = 643000 * Double.valueOf(getBalance(address));
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
  public List<TransactionStatus> getTransactionList(String address) {
    List<TransactionStatus> txStatusList = new ArrayList<>();
    createContext().selectFrom(TRANSACTION).where(TRANSACTION.SOURCEADDRESS.eq(address).or(TRANSACTION.TARGETADDRESS.eq(address))).fetch().forEach(r->{
      TransactionStatus txStatus = new TransactionStatus();
      txStatus.setAmount(r.getAmount());
      txStatus.setSourceAddress(r.getValue(TRANSACTION.SOURCEADDRESS));
      txStatus.setTargetAddress(r.getValue(TRANSACTION.TARGETADDRESS));
      txStatus.setTransactionHash(r.getValue(TRANSACTION.HASH));
      txStatusList.add(txStatus);
    });
    return txStatusList;
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
