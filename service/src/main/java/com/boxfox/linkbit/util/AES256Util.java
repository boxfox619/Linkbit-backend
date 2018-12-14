package com.boxfox.linkbit.util;

import com.boxfox.vertx.data.Config;
import com.boxfox.vertx.secure.AES256;

import java.io.UnsupportedEncodingException;

public class AES256Util {
    private static AES256 instance;

    public static AES256 getInstance() {
        if (instance == null) {
            try {
                String key = Config.getDefaultInstance().getString("aesKey");
                instance = AES256.create(key);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static String encrypt(String str){
        return getInstance().encrypt(str);
    }

    public static String decrypt(String str){
        return getInstance().decrypt(str);
    }
}
