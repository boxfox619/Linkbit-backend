package com.boxfox.linkbit.util;

import static com.boxfox.cross.service.network.RequestService.request;

import io.vertx.core.json.JsonArray;

public class ERC20Tokens {

  private static final String TOKEN_INFO_URL = "https://raw.githubusercontent.com/kvhnuke/etherwallet/v3.10.4.3/app/scripts/tokens/ethTokens.json";
  private JsonArray tokens;

  private ERC20Tokens() {
    request(TOKEN_INFO_URL, event -> {
      if (event.succeeded()) {
        tokens = new JsonArray(event.result());
      }
    });
  }

  private static class Instance {
    private static ERC20Tokens erc20Tokens = new ERC20Tokens();
  }

  public static String getTokenAddress(String symbol) {
    for (int i = 0; i < Instance.erc20Tokens.tokens.size(); i++) {
      if (Instance.erc20Tokens.tokens.getJsonObject(i).getString("symbol")
          .equals(symbol.toUpperCase())) {
        return Instance.erc20Tokens.tokens.getJsonObject(i).getString("address");
      }
    }
    return null;
  }

}
