package org.hfut.work.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate redis;

    // String get/set
    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    public void set(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    public void set(String key, String value, long ttlSeconds) {
        redis.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public void set(String key, String value, Duration ttl) {
        redis.opsForValue().set(key, value, ttl);
    }

    public Boolean setIfAbsent(String key, String value, Duration ttl) {
        return redis.opsForValue().setIfAbsent(key, value, ttl);
    }

    public String getAndSet(String key, String value) {
        return redis.opsForValue().getAndSet(key, value);
    }

    // Key ops
    public Boolean delete(String key) {
        return redis.delete(key);
    }

    public Boolean hasKey(String key) {
        return redis.hasKey(key);
    }

    public Boolean expire(String key, long ttlSeconds) {
        return redis.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    public Long ttl(String key) {
        return redis.getExpire(key, TimeUnit.SECONDS);
    }

    // Counters
    public Long incr(String key) {
        return redis.opsForValue().increment(key);
    }

    public Long incrBy(String key, long delta) {
        return redis.opsForValue().increment(key, delta);
    }

    public Long decr(String key) {
        return redis.opsForValue().decrement(key);
    }

    public Long decrBy(String key, long delta) {
        return redis.opsForValue().decrement(key, delta);
    }
}


