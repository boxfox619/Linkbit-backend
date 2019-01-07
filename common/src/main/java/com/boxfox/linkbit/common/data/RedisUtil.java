package com.boxfox.linkbit.common.data;

import com.boxfox.vertx.data.Config;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

public class RedisUtil {

    public static JedisPool createPool() {
        String host = Config.getDefaultInstance().getString("redisHost", "127.0.0.1");
        int port = Config.getDefaultInstance().getInt("redisPort", 6379);
        String password = Config.getDefaultInstance().getString("redisPassword");

        GenericObjectPoolConfig poolConfig =new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(3);
        poolConfig.setMaxWaitMillis(3000);
        poolConfig.setTestOnBorrow(true);

        return new JedisPool(poolConfig, host, port, 1000, password);
    }

}
