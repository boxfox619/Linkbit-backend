package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.AbstractRouter;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.service.ShareService;
import com.boxfox.cross.service.model.ShareContent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.io.File;

public class WalletShareRouter extends AbstractRouter{

    @Service
    private ShareService shareService;
    @Service
    private AddressService addressService;

    @RouteRegistration(uri="/share/send", method=HttpMethod.GET)
    public void connectShareLink(RoutingContext ctx, @Param String data){
        ctx.response().end(shareService.createTransactionHtml(data));
    }

    @RouteRegistration(uri="/share/decode", method=HttpMethod.GET)
    public void decodeTransactionData(RoutingContext ctx, @Param String data){
        ShareContent content = shareService.decodeTransactionData(data);
        if(content!=null){
            ctx.response().end(gson.toJson(content));
        }else{
            ctx.response().setStatusMessage("wrong transaction data").setStatusCode(400);
        }
        ctx.response().end();
    }

    @RouteRegistration(uri="/share/qr", method=HttpMethod.GET)
    public void createQrCode(RoutingContext ctx, @Param String address, @Param int amount){
        addressService.findByAddress(address, res->{
            if(res.result() != null) {
                String urlPrefix = ctx.request().uri().replace(ctx.currentRoute().getPath(), "");
                String data = shareService.createTransactionData(res.result().getSymbol(), address, amount);
                String url = urlPrefix + data;
                File qrFile = shareService.createQRImage(url);
                ctx.response().sendFile(qrFile.getName());
                ctx.response().closeHandler((e) -> {
                    qrFile.delete();
                });
            }else{
                ctx.fail(400);
            }
        });

    }

    @RouteRegistration(uri="/share/link", method = HttpMethod.POST)
    public void createLink(RoutingContext ctx, @Param String address, @Param int amount){
        addressService.findByAddress(address, res -> {
            if(res.result() != null) {
                String urlPrefix = ctx.request().uri().replace(ctx.currentRoute().getPath(), "");
                String data = shareService.createTransactionData(res.result().getSymbol(), address, amount);
                ctx.response().end(urlPrefix+"/"+data);
            }else{
                ctx.fail(400);
            }
        });
    }

}
