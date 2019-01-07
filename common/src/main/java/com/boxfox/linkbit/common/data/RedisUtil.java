package com.boxfox.linkbit.common.data;

import com.boxfox.vertx.data.Config;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

public class RedisUtil {

    public static JedisCluster create() {
        String host = Config.getDefaultInstance().getString("redisHost", "127.0.0.1");
        int port = Config.getDefaultInstance().getInt("redisPort", 6379);
        String password = Config.getDefaultInstance().getString("redisPassword");
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        jedisClusterNodes.add(new HostAndPort(host, port));

        GenericObjectPoolConfig poolConfig =new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(3);
        poolConfig.setMaxWaitMillis(3000);
        poolConfig.setTestOnBorrow(true);
        JedisCluster jedis = new JedisCluster(jedisClusterNodes, 5000, 100, 10, password, poolConfig);
        return jedis;
    }

}
