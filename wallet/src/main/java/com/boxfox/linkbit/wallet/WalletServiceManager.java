package com.boxfox.linkbit.wallet;

import com.boxfox.linkbit.wallet.eth.EthereumServicePackage;
import io.vertx.core.Vertx;

import java.util.HashMap;
import java.util.Map;

public class WalletServiceManager {
    private Map<String, WalletService> serviceMap;

    private WalletServiceManager() {
        serviceMap = new HashMap<>();
    }

    public static class CryptoCurrencyRegisteryInstance {
        private static WalletServiceManager instance = new WalletServiceManager();
    }

    public static WalletServiceManager getInstance() {
        return CryptoCurrencyRegisteryInstance.instance;
    }

    public static void register(WalletService service) {
        getInstance().serviceMap.put(service.symbol, service);
    }

    public static void register(String[] symbols, WalletService service) {
        for (String symbol : symbols) {
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
        if (name != null)
            return getInstance().serviceMap.get(name.toUpperCase());
        return null;
    }

    public static void init(Vertx vertx) {
        register(EthereumServicePackage.create(vertx));
    }
}
