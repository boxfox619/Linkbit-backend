package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.ShareService;
import com.boxfox.cross.service.model.ShareContent;
import com.boxfox.cross.service.model.Wallet;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.io.File;

public class WalletShareRouter extends WalletRouter{
    private ShareService shareService;

    public WalletShareRouter(){
        super();
        this.shareService = new ShareService();
    }

    @RouteRegistration(uri="/deposit/transaction/:data", method=HttpMethod.GET)
    public void connectShareLink(RoutingContext ctx, String data){
        ctx.response().end(shareService.createTransactionHtml(data));
    }

    @RouteRegistration(uri="/transaction/share/:data", method=HttpMethod.GET)
    public void decodeTransactionData(RoutingContext ctx, String data){
        ShareContent content = shareService.decodeTransactionData(data);
        if(content!=null){
            ctx.response().end(gson.toJson(content));
        }else{
            ctx.response().setStatusMessage("wrong transaction data").setStatusCode(400);
        }
        ctx.response().end();
    }

    @RouteRegistration(uri="/transaction/share/qr", method=HttpMethod.GET)
    public void createQrCode(RoutingContext ctx, @Param String symbol, @Param String address, @Param int amount){
        Wallet wallet = addressService.findByAddress(symbol, address);
        if(wallet != null) {
            String urlPrefix = ctx.request().uri().replace(ctx.currentRoute().getPath(), "");
            String data = shareService.createTransactionData(symbol, address, amount);
            String url = urlPrefix + data;
            File qrFile = shareService.createQRImage(url);
            ctx.response().sendFile(qrFile.getName());
            ctx.response().closeHandler((e) -> {
                qrFile.delete();
            });
        }else{
            ctx.fail(400);
        }

    }

    @RouteRegistration(uri="/transaction/share/link/:symbol", method = HttpMethod.POST)
    public void createLink(RoutingContext ctx, @Param String symbol, @Param String address, @Param int amount){
        Wallet wallet = addressService.findByAddress(symbol, address);
        if(wallet != null) {
            String urlPrefix = ctx.request().uri().replace(ctx.currentRoute().getPath(), "");
            String data = shareService.createTransactionData(symbol, address, amount);
            ctx.response().end(urlPrefix+"/"+data);
        }else{
            ctx.fail(400);
        }
    }

}
