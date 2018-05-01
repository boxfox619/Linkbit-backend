package com.boxfox.core.router;

import com.boxfox.support.data.Config;
import com.boxfox.support.vertx.router.RouteRegistration;
import com.boxfox.support.vertx.router.Param;
import com.google.common.io.Files;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class WalletRouter {
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
        ctx.response().write(result.encode());
        ctx.response().end();
    }
}
