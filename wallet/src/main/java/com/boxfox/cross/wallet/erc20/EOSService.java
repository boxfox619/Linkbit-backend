package com.boxfox.cross.wallet.erc20;

import com.boxfox.cross.service.PriceService;
import com.boxfox.cross.wallet.ERC20Service;
import io.vertx.core.Vertx;

public class EOSService  extends ERC20Service{

    public EOSService(Vertx vertx){
        super(vertx,"EOS");
    }

    @Override
    public double getPrice(String address) {
        String balance = getBalance(address);
        double price = 0;
        if(balance!=null){
            price = Double.parseDouble(balance) * PriceService.getPrice("EOS");
        }
        return price;
    }
}
