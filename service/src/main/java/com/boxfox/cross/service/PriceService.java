package com.boxfox.cross.service;

import java.util.HashMap;
import java.util.Map;

public class PriceService {
    private Map<String, Integer> priceMap;

    private PriceService(){
        this.priceMap = new HashMap<>();
        this.priceMap.put("ETH", 643000);
        this.priceMap.put("EOS", 13490);
        this.priceMap.put("OMG", 11930);
    }

    private static class PriceServiceInstance{
        private static PriceService instance = new PriceService();
    }

    public static int getPrice(String symbol){
        return PriceServiceInstance.instance.priceMap.get(symbol);
    }
}
