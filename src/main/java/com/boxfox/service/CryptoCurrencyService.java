package com.boxfox.service;

import io.vertx.core.json.JsonObject;

public interface CryptoCurrencyService {

  String getBalance(String address);
  JsonObject createWallet(String password);
  JsonObject send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);
}
