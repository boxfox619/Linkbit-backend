package com.boxfox.service;

import java.util.HashMap;
import java.util.Map;

public class CryptoCurrencyManager {
  private Map<String, CryptoCurrencyService> serviceMap;

  private CryptoCurrencyManager(){
    serviceMap = new HashMap<>();
  }

  public static class CryptoCurrencyRegisteryInstance{
    private static CryptoCurrencyManager instance = new CryptoCurrencyManager();
  }

  public static CryptoCurrencyManager getInstance(){
    return CryptoCurrencyRegisteryInstance.instance;
  }

  public static void register(String name, CryptoCurrencyService service){
    getInstance().serviceMap.put(name, service);
  }

  public static void unRegister(String name){
    getInstance().serviceMap.remove(name);
  }

  public static boolean checkValid(String name){
    return getInstance().serviceMap.containsKey(name);
  }

  public static CryptoCurrencyService getService(String name){
    return getInstance().serviceMap.get(name);
  }
}
