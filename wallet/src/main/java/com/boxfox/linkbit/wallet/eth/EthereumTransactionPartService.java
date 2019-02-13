package com.boxfox.linkbit.wallet.eth;

import com.boxfox.linkbit.common.RoutingException;
import com.boxfox.linkbit.common.data.PostgresConfig;
import com.boxfox.linkbit.common.entity.transaction.TransactionModel;
import com.boxfox.linkbit.wallet.WalletServiceException;
import com.boxfox.linkbit.wallet.model.TransactionResult;
import com.boxfox.linkbit.wallet.part.TransactionPart;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.io.Files;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
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

public class EthereumTransactionPartService extends EthereumPart implements TransactionPart {

    static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
    private static final String HTTP_API_ETHERSCAN_IO_API_TXLIST = "http://api.etherscan.io/api?module=account&action=txlist&startblock=%s&endblock=99999999&sort=asc&apikey=WN69XKERKW2UYW3QM3YPKFD4VUJCPE1NVM&address=%s";

    public EthereumTransactionPartService(Vertx vertx, Web3j web3, File cachePath) {
        super(vertx, web3, cachePath);
    }

    @Override
    public TransactionResult send(JsonObject data, String targetAddress, String amount) {
        TransactionResult result = new TransactionResult();
        try {
            Credentials credentials = this.getCredentials(data);
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            BigInteger amountEth = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, GAS_PRICE, GAS_LIMIT, targetAddress, amountEth);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Credentials getCredentials(JsonObject data) throws IOException, CipherException {
        switch (data.getString("type")) {
            case "mnemonic":
                return WalletUtils.loadBip39Credentials(data.getString("password"), data.getString("mnemonic"));
            case "privateKey":
                return Credentials.create(data.getString("privateKey"));
            case "keystore":
                File tmpWallet = new File(cachePath.getPath() + File.separator + System.currentTimeMillis());
                Files.write(data.getJsonObject("keystore").toString(), tmpWallet, Charset.forName("UTF-8"));
                return WalletUtils.loadCredentials(data.getString("password"), tmpWallet.getPath());
        }
        throw new IllegalArgumentException("type not match");
    }

    @Override
    public List<TransactionModel> getTransactionList(String address) throws RoutingException {
        try {
            int totalBlockNumber = web3.ethBlockNumber().send().getBlockNumber().intValue();
            List<TransactionModel> txStatusList = new ArrayList<>();
            TransactionDao transactionDao = new TransactionDao(PostgresConfig.create());
            WalletDao walletDao = new WalletDao(PostgresConfig.create());
            List<io.one.sys.db.tables.pojos.Transaction> transactions = new ArrayList<>();
            transactions.addAll(transactionDao.fetchByTargetaddress());
            transactions.addAll(transactionDao.fetchBySourceaddress());
            for (io.one.sys.db.tables.pojos.Transaction tx : transactions) {
                TransactionModel txStatus = new TransactionModel();
                txStatus.setHash(tx.getHash());
                txStatus.setAmount(tx.getAmount());
                txStatus.setSourceAddress(tx.getSourceaddress());
                txStatus.setTargetAddress(tx.getTargetaddress());
                txStatus.setDate(tx.getDatetime());
                Wallet wallet = walletDao.findById(tx.getTargetaddress());
                if (wallet != null) {
                    txStatus.setTargetProfile(wallet.getName());
                } else {
                    txStatus.setTargetProfile("Unknown");
                }
                //@TODO blocknumber, status save to db
                TransactionReceipt receipt = web3.ethGetTransactionReceipt(tx.getHash()).send().getTransactionReceipt().get();
                txStatus.setStatus(receipt.getStatus().equals("0x1"));
                txStatus.setBlock(receipt.getBlockNumber().intValue());
                txStatus.setConfirm(totalBlockNumber - txStatus.getBlock());
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
            status.setConfirm(confirmation.intValue());
            status.setSourceAddress(receipt.getFrom());
            status.setTargetAddress(receipt.getTo());
            status.setHash(receipt.getBlockHash());
            status.setAmount(Double.valueOf(Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER).toPlainString()));
            status.setStatus(receipt.getStatus().equals("0x1"));
            status.setBlock(receipt.getBlockNumber().intValue());
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
            return web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().getTransactionCount().intValue();
        } catch (IOException e) {
            throw new WalletServiceException("Can not get transaction count");
        }
    }

    @Override
    public void indexingTransaction(String address) {
        try {
            String result = Unirest.get(String.format(HTTP_API_ETHERSCAN_IO_API_TXLIST, 0, address)).asString().getBody();
            JsonObject obj = new JsonObject(result);
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
                TransactionDao dao = new TransactionDao(PostgresConfig.create());
                io.one.sys.db.tables.pojos.Transaction transaction = new io.one.sys.db.tables.pojos.Transaction();
                transaction.setHash(hash);
                transaction.setSourceaddress(from);
                transaction.setTargetaddress(to);
                transaction.setAmount(Double.valueOf(amount.toPlainString()));
                transaction.setDatetime(dateTime);
                dao.insert(transaction);
            }
        } catch (UnirestException e) {
            Logger.getRootLogger().error(e);
        }
    }

    @Override
    public List<TransactionModel> indexingTransactions(String address, int fromBlockNumber) throws WalletServiceException, RoutingException {
        List<TransactionModel> list = new ArrayList<>();
        try {
            String result = Unirest.get(String.format(HTTP_API_ETHERSCAN_IO_API_TXLIST, fromBlockNumber, address)).asString().getBody();
            JsonObject obj = new JsonObject(result);
            JsonArray transactions = obj.getJsonArray("result");
            for (int i = 0; i < transactions.size(); i++) {
                JsonObject tx = transactions.getJsonObject(i);
                String hash = tx.getString("hash");
                String from = tx.getString("from");
                String blockNumber = tx.getString("blockNumber");
                String to = tx.getString("to");
                String receipt = tx.getString("txreceipt_status");
                String confirmations = tx.getString("confirmations");
                String value = tx.getString("value");
                String timeStamp = tx.getString("timeStamp");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Integer.valueOf(timeStamp));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss");
                String dateTime = simpleDateFormat.format(timestamp);
                BigDecimal amount = Convert.fromWei(value, Convert.Unit.ETHER);
                TransactionModel status = new TransactionModel();
                status.setHash(hash);
                status.setSourceAddress(from);
                status.setTargetAddress(to);
                status.setAmount(amount.doubleValue());
                status.setDate(dateTime);
                status.setConfirm(Integer.valueOf(confirmations));
                status.setStatus(receipt.equals("1"));
                status.setBlock(Integer.valueOf(blockNumber));
                list.add(status);
            }
        } catch (UnirestException e) {
            Logger.getRootLogger().error(e);
        }
        return list;
    }

}
