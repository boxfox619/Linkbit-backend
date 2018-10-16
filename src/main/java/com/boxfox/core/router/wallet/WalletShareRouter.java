package com.boxfox.core.router.wallet;

import com.boxfox.vertx.router.*;
import com.boxfox.vertx.service.*;
import com.boxfox.cross.service.ShareService;
import com.boxfox.cross.service.WalletDatabaseService;
import com.boxfox.cross.entity.ShareContent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.io.File;

import static com.boxfox.cross.util.LogUtil.getLogger;

public class WalletShareRouter extends AbstractRouter {

    @Service
    private ShareService shareService;
    @Service
    private WalletDatabaseService walletDatabaseService;

    @RouteRegistration(uri = "/share/send", method = HttpMethod.GET)
    public void connectShareLink(RoutingContext ctx, @Param(name = "data") String data) {
        ctx.response().end(shareService.createTransactionHtml(data));
    }

    @RouteRegistration(uri = "/share/decode", method = HttpMethod.GET)
    public void decodeTransactionData(RoutingContext ctx, @Param(name = "data") String data) {
        ShareContent content = shareService.decodeTransactionData(data);
        if (content != null) {
            ctx.response().end(gson.toJson(content));
        } else {
            ctx.response().setStatusMessage("wrong transaction data").setStatusCode(400);
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/share/qr", method = HttpMethod.GET)
    public void createQrCode(RoutingContext ctx, @Param(name = "address") String address, @Param(name = "amount") int amount) {
        getLogger().debug(String.format("Create QR Code %s %s", address, amount));
        walletDatabaseService.findByAddress(address, res -> {
            if (res.result() != null) {
                String urlPrefix = ctx.request().uri().replace(ctx.currentRoute().getPath(), "");
                String data = shareService.createTransactionData(res.result().getCoinSymbol(), address, amount);
                String url = urlPrefix + data;
                File qrFile = shareService.createQRImage(url);
                ctx.response().sendFile(qrFile.getName());
                ctx.response().closeHandler((e) -> qrFile.delete());
            } else {
                ctx.fail(400);
            }
        });

    }

    @RouteRegistration(uri = "/share/link", method = HttpMethod.POST)
    public void createLink(RoutingContext ctx, @Param(name = "address") String address, @Param(name = "amount") int amount) {
        walletDatabaseService.findByAddress(address, res -> {
            if (res.result() != null) {
                String urlPrefix = ctx.request().uri().replace(ctx.currentRoute().getPath(), "");
                String data = shareService.createTransactionData(res.result().getCoinSymbol(), address, amount);
                ctx.response().end(urlPrefix + "/" + data);
            } else {
                ctx.fail(400);
            }
        });
    }

}
