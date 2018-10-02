package com.boxfox.cross.wallet.eth;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.wallet.indexing.IndexingService;
import io.one.sys.db.tables.daos.TransactionDao;
import io.one.sys.db.tables.pojos.Transaction;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;

import static com.boxfox.cross.service.network.RequestService.request;

public class EthIndexingService implements IndexingService {
    private Vertx vertx;
    private Web3j web3;
    private static final String URL = "http://api.etherscan.io/api?module=account&action=txlist&startblock=0&endblock=99999999&sort=asc&apikey=WN69XKERKW2UYW3QM3YPKFD4VUJCPE1NVM";

    protected EthIndexingService(Web3j web3, Vertx vertx) {
        this.web3 = web3;
        this.vertx = vertx;
    }

    @Override
    public Future<Void> indexing(Vertx vertx, String address) {
        Future future = Future.future();
        request(URL + "&address=" + address,e -> {
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
                    Transaction transaction = new Transaction();
                    transaction.setHash(hash);
                    transaction.setSourceaddress(from);
                    transaction.setTargetaddress(to);
                    transaction.setAmount(Double.valueOf(amount.toPlainString()));
                    transaction.setDatetime(dateTime);
                    dao.insert(transaction);
                }
                future.complete();
            } else {
                future.fail(e.cause());
            }
        });
        return future;
    }
}
