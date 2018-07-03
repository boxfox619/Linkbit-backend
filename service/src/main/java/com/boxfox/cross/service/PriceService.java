package com.boxfox.cross.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.vertx.core.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PriceService {
    private static final String COINMARKET_CAP_URL = "https://api.coinmarketcap.com/v2/";
    private Map<String, Integer> coinIdMap;

    private PriceService(){
        this.coinIdMap = new HashMap<>();
        try {
            JSONArray array = Unirest.get(COINMARKET_CAP_URL + "listings/").asJson().getBody().getObject().getJSONArray("data");
            array.forEach(obj -> {
                JSONObject coin = (JSONObject)obj;
                String symbol = coin.getString("symbol");
                int id = coin.getInt("id");
                coinIdMap.put(symbol, id);
            });
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private static class PriceServiceInstance{
        private static PriceService instance = new PriceService();
    }

    public static double getPrice(String symbol){
        PriceService service = PriceServiceInstance.instance;
        symbol = symbol.toUpperCase();
        if(service.coinIdMap.containsKey(symbol)){
            try {
                int id = service.coinIdMap.get(symbol);
                JSONObject obj = Unirest.get(COINMARKET_CAP_URL+"ticker/"+id+"/?convert=KRW").asJson().getBody().getObject();
                JSONObject krw = obj.getJSONObject("data").getJSONObject("quotes").getJSONObject("KRW");
                return krw.getDouble("price");
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
