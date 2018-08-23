package com.boxfox.cross.service;

import com.boxfox.cross.common.vertx.service.AbstractService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PriceService extends AbstractService {
    private static final String COINMARKET_CAP_URL = "https://api.coinmarketcap.com/v2/ticker/";
    private Map<String, Integer> coinIdMap;

    private PriceService() {
        this.coinIdMap = new HashMap<>();
        try {
            JSONArray array = Unirest.get(COINMARKET_CAP_URL + "listings/").asJson().getBody().getObject().getJSONArray("data");
            array.forEach(obj -> {
                JSONObject coin = (JSONObject) obj;
                String symbol = coin.getString("symbol");
                int id = coin.getInt("id");
                coinIdMap.put(symbol, id);
            });
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getUnit(String locale){
        //@TODO Implemnt money symbol
        return "";
    }

    public double getPrice(String symbol, String moneySymbol) {
        symbol = symbol.toUpperCase();
        if (coinIdMap.containsKey(symbol)) {
            try {
                int id = coinIdMap.get(symbol);
                String url = String.format("%s%s/?convert=", COINMARKET_CAP_URL, id, moneySymbol);
                JSONObject obj = Unirest.get(url).asJson().getBody().getObject();
                JSONObject krw = obj.getJSONObject("data").getJSONObject("quotes").getJSONObject(moneySymbol);
                return krw.getDouble("price");
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
