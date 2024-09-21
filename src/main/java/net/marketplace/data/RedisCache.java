package net.marketplace.data;

import redis.clients.jedis.Jedis;

public class RedisCache {

    private Jedis jedis;

    public RedisCache() {
        jedis = new Jedis("localhost", 6379);  // Connect to Redis
    }

    public String getItemCache(String key) {
        return jedis.get(key);
    }

    public void setItemCache(String key, String value) {
        jedis.set(key, value);
    }

    public void close() {
        jedis.close();
    }
}
