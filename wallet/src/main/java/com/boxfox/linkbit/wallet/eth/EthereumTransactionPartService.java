package com.boxfox.linkbit.wallet.eth;

import com.boxfox.cross.common.RoutingException;
import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.linkbit.wallet.WalletServiceException;
import com.boxfox.linkbit.wallet.model.TransactionResult;
import com.boxfox.linkbit.wallet.part.TransactionPart;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.io.Files;
import com.linkbit.android.entity.TransactionModel;
import io.one.sys.db.tables.daos.TransactionDao;
import io.one.sys.db.tables.daos.WalletDao;
import io.one.sys.db.tables.pojos.Wallet;
import io.vertx.core.Vertx;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
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
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import static com.boxfox.cross.service.network.RequestService.request;

public class EthereumTransactionPartService extends EthereumPart implements TransactionPart {

  static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
  static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
  private static final String HTTP_API_ETHERSCAN_IO_API_TXLIST = "http://api.etherscan.io/api?module=account&action=txlist&startblock=0&endblock=99999999&sort=asc&apikey=WN69XKERKW2UYW3QM3YPKFD4VUJCPE1NVM";

  public EthereumTransactionPartService(Vertx vertx, Web3j web3, File cachePath) {
    super(vertx, web3, cachePath);
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
  public List<TransactionModel> getTransactionList(String address) throws RoutingException {
      try {
        int totalBlockNumber = web3.ethBlockNumber().send().getBlockNumber().intValue();
        List<TransactionModel> txStatusList = new ArrayList<>();
        TransactionDao transactionDao = new TransactionDao(PostgresConfig.create(),vertx);
        WalletDao walletDao = new WalletDao(PostgresConfig.create(),vertx);
        List<io.one.sys.db.tables.pojos.Transaction> transactions = new ArrayList<>();
        transactionDao.findManyByTargetaddress(Arrays.asList(address)).result().forEach(t -> transactions.add(t));
        transactionDao.findManyBySourceaddress(Arrays.asList(address)).result().forEach(t -> transactions.add(t));
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
          //@TODO blocknumber, status save to db
          TransactionReceipt receipt = web3.ethGetTransactionReceipt(tx.getHash()).send().getTransactionReceipt().get();
          txStatus.setStatus(receipt.getStatus().equals("0x1"));
          txStatus.setBlockNumber(receipt.getBlockNumber().intValue());
          txStatus.setConfirmation(totalBlockNumber - txStatus.getBlockNumber());
          txStatusList.add(txStatus);
        }
        return txStatusList;
      } catch (IOException e) {
        e.printStackTrace();
        throw new RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
      }
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

  @Override
  public void indexingTransaction(String address){
    request(HTTP_API_ETHERSCAN_IO_API_TXLIST + "&address=" + address, e -> {
      if (e.succeeded()) {
        JsonObject obj = new JsonObject(e.result());
        JsonArray transactions = obj.getJsonArray("result");
        for (int i = 0; i < transactions.size(); i++) {
          JsonObject tx = transactions.getJsonObject(i);
          String hash = tx.getString("hash");
          String from = tx.getString("from");
          String to = tx.getString("to");
          String value = tx.getString("value");
          String timeStamp = tx.getString("timeStamp");
          Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Integer.valueOf(timeStamp));
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss");
          String dateTime = simpleDateFormat.format(timestamp);
          BigDecimal amount = Convert.fromWei(value, Convert.Unit.ETHER);
          TransactionDao dao = new TransactionDao(PostgresConfig.create(), this.vertx);
          io.one.sys.db.tables.pojos.Transaction transaction = new io.one.sys.db.tables.pojos.Transaction();
          transaction.setHash(hash);
          transaction.setSourceaddress(from);
          transaction.setTargetaddress(to);
          transaction.setAmount(Double.valueOf(amount.toPlainString()));
          transaction.setDatetime(dateTime);
          dao.insert(transaction);
        }
      } else {
        Logger.getRootLogger().error(e.cause());
      }
    });
  }

}
