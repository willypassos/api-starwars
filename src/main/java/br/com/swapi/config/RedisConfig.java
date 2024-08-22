package br.com.swapi.config;

import redis.clients.jedis.Jedis;

public class RedisConfig {
    private static Jedis jedis;

    static {
        jedis = new Jedis("localhost", 6379);
    }

    public static Jedis getJedis() {
        return jedis;
    }

    public static void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
