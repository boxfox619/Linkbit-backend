package com.boxfox.linkbit.wallet;

import com.boxfox.linkbit.util.ERC20Tokens;
import com.boxfox.linkbit.wallet.eth.EthereumServiceContext;
import io.vertx.core.Vertx;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class WalletServiceRegistry {

  private Map<String, WalletService> serviceMap;

  public static void init(Vertx vertx) {
    Logger.getRootLogger().info("Start wallet services initializing");
    register(EthereumServiceContext.create(vertx));
    ERC20Tokens.init();
    Logger.getRootLogger().info("Finish wallet services initialization");
  }

  private WalletServiceRegistry() {
    serviceMap = new HashMap<>();
  }

  public static class WalletServiceRegistryInstance {

    private static WalletServiceRegistry instance = new WalletServiceRegistry();
  }

  public static WalletServiceRegistry getInstance() {
    return WalletServiceRegistryInstance.instance;
  }

  public static void register(WalletServiceContext context) {
    getInstance().serviceMap.put(context.symbol, new WalletService(context));
  }

  public static void register(String[] symbols, WalletServiceContext context) {
    for (String symbol : symbols) {
      WalletService service = new WalletService(context);
      service.init();
      getInstance().serviceMap.put(symbol, service);
    }
  }

  public static void unRegister(String name) {
    getInstance().serviceMap.remove(name);
  }

  public static boolean checkValid(String name) {
    return getInstance().serviceMap.containsKey(name);
  }

  public static WalletService getService(String name) {
    if (name != null) {
      return getInstance().serviceMap.get(name.toUpperCase());
    }
    return null;
  }

}
