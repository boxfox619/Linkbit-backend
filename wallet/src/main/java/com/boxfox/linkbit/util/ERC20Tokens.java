package com.boxfox.linkbit.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;

public class ERC20Tokens {

    private static final String TOKEN_INFO_URL = "https://raw.githubusercontent.com/kvhnuke/etherwallet/v3.10.4.3/app/scripts/tokens/ethTokens.json";
    private JSONArray tokens;

    public static void init() {
        try {
            Instance.erc20Tokens.tokens = Unirest.get(TOKEN_INFO_URL).asJson().getBody().getArray();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private static class Instance {
        private static ERC20Tokens erc20Tokens = new ERC20Tokens();
    }

    public static String getTokenAddress(String symbol) {
        for (int i = 0; i < Instance.erc20Tokens.tokens.length(); i++) {
            if (Instance.erc20Tokens.tokens.getJSONObject(i).getString("symbol")
                    .equals(symbol.toUpperCase())) {
                return Instance.erc20Tokens.tokens.getJSONObject(i).getString("address");
            }
        }
        return null;
    }

}
