package com.boxfox.core.router;

import com.boxfox.service.CryptoCurrencyManager;
import com.boxfox.service.CryptoCurrencyService;
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


    @RouteRegistration(uri = "/wallet:name//balance", method = HttpMethod.GET)
    public void getBalance(RoutingContext ctx, @Param String address) {
        String name = ctx.pathParam("name");
        CryptoCurrencyService service = CryptoCurrencyManager.getService(name);
        String balance = service.getBalance(address);
        if(balance==null){
            ctx.response().write(balance);
            ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }else{
            ctx.response().write(balance);
            ctx.response().setStatusCode(200);
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/:name/create", method = HttpMethod.POST)
    public void create(RoutingContext ctx, @Param String password) {
        String name = ctx.pathParam("name");
        CryptoCurrencyService service = CryptoCurrencyManager.getService(name);
        JsonObject result = service.createWallet(password);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet:name//send", method = HttpMethod.POST)
    public void send(RoutingContext ctx,
                     @Param String walletFileName,
                     @Param String walletJsonFile,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        String name = ctx.pathParam("name");
        CryptoCurrencyService service = CryptoCurrencyManager.getService(name);
        JsonObject result = service.send(walletFileName, walletJsonFile, password, targetAddress, amount);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }
}
