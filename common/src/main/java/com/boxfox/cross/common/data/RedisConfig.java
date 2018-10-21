package com.boxfox.cross.common.data;

import com.boxfox.vertx.data.Config;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    public static JedisPoolConfig create() {
        JedisPoolConfig config = new JedisPoolConfig();
        return config;
    }

    public static JedisPool createPool() {
        JedisPool pool = new JedisPool(create(), Config.getDefaultInstance().getString("redisHost", "127.0.0.1"));
        return pool;
    }
}
