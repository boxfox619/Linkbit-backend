package com.boxfox.cross.service;

import com.boxfox.cross.common.vertx.service.AbstractService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.boxfox.cross.common.vertx.service.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class PriceService extends AbstractService {
    private static final String COINMARKET_CAP_URL = "https://api.coinmarketcap.com/v2/ticker/";
    private Map<String, Integer> coinIdMap;

    @Service
    LocaleService localeService; //@TODO Service inject to Service

    public PriceService() {
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

    public double getPrice(String symbol, double balance) {
        return getPrice(symbol, Locale.KOREA.toString(), balance);
    }

    public double getPrice(String symbol, String locale, double balance) {
        double price = getPrice(symbol, locale);
        if (price > 0) {
            return price * balance; //@TODO dobule calculate improve
        } else {
            return -1;
        }
    }

    public double getPrice(String symbol) {
        return getPrice(symbol, Locale.KOREA.toString());
    }

    public double getPrice(String symbol, String locale) {
        String moneySymbol = localeService.getLocaleMoneySymbol(locale);
        symbol = symbol.toUpperCase();
        if (coinIdMap.containsKey(symbol)) {
            try {
                int id = coinIdMap.get(symbol);
                String url = String.format("%s%s/?convert=%s", COINMARKET_CAP_URL, id, moneySymbol);
                JSONObject obj = Unirest
                        .get(url).asJson()
                        .getBody().getObject();
                JSONObject krw = obj.getJSONObject("data").getJSONObject("quotes").getJSONObject(moneySymbol);
                return krw.getDouble("price");
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
