package com.boxfox.core.router;

import com.boxfox.support.data.Config;
import com.boxfox.support.vertx.router.RouteRegistration;
import com.boxfox.support.vertx.router.Param;
import com.google.common.io.Files;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

public class WalletRouter {
    static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    private Web3j web3;
    private File cachePath;

    public WalletRouter(){
        this.web3 =  Web3j.build(new HttpService("https://mainnet.infura.io/JjSRoXryXbE6HgXJGILz"));
        this.cachePath = new File(Config.getDefaultInstance().getString("walletCachePath"));
        if(!cachePath.exists())
            cachePath.mkdirs();
    }

    @RouteRegistration(uri = "/wallet/balance", method = HttpMethod.GET)
    public void getBalance(RoutingContext ctx, @Param String address) {
        try {
            String balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance().toString();
            ctx.response().write(balance);
            ctx.response().setStatusCode(200);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/create", method = HttpMethod.POST)
    public void create(RoutingContext ctx, @Param String password) {
        JsonObject result = new JsonObject();
        result.put("result", false);
        try {
            String walletFileName = WalletUtils.generateFullNewWalletFile(password, cachePath);
            File jsonFile = new File(cachePath.getPath() + File.separator + walletFileName);
            String walletJson = Files.toString(jsonFile, Charset.defaultCharset());
            jsonFile.delete();
            result.put("result", true);
            result.put("name", walletFileName);
            result.put("wallet", new JsonObject(walletJson));
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
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/send", method = HttpMethod.POST)
    public void send(RoutingContext ctx,
                     @Param String walletFileName,
                     @Param String walletJsonFile,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        File tmpWallet = new File(cachePath.getPath()+File.separator+walletFileName);
        JsonObject result = new JsonObject();
        result.put("result", false);
        try {
            Files.write(walletJsonFile, tmpWallet, Charset.forName("UTF-8"));
            Credentials credentials1 = WalletUtils.loadCredentials(password, tmpWallet.getPath());
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                    credentials1.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            BigInteger amountEth = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction (
                    nonce, GAS_PRICE, GAS_LIMIT, targetAddress, amountEth);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials1);
            String hexValue = Numeric.toHexString(signedMessage);

            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();
            result.put("result", true);
            result.put("transaction", transactionHash);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }
}
