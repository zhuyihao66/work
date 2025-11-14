package org.hfut.work.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Random;

@Service
public class OtpService {
    private static final String OTP_PREFIX = "otp:";
    private static final String FAIL_PREFIX = "otp:fail:";
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final Random RANDOM = new Random();
    private static final int MAX_ATTEMPTS = 3;

    @Resource
    private StringRedisTemplate redis;

    public String generateAndStore(String phone) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        redis.opsForValue().set(OTP_PREFIX + phone, code, TTL);
        // reset fail counter
        redis.delete(FAIL_PREFIX + phone);
        return code;
    }

    public static class VerifyResult {
        public boolean success;
        public boolean locked;
        public int remaining; // remaining attempts before lock
    }

    public VerifyResult verifyWithLimit(String phone, String code) {
        VerifyResult res = new VerifyResult();
        String key = OTP_PREFIX + phone;
        String val = redis.opsForValue().get(key);
        if (val == null) {
            res.success = false;
            res.locked = true; // treat as invalid/expired
            res.remaining = 0;
            return res;
        }
        if (val.equals(code)) {
            redis.delete(key);
            redis.delete(FAIL_PREFIX + phone);
            res.success = true;
            res.locked = false;
            res.remaining = MAX_ATTEMPTS;
            return res;
        }
        // wrong code: increment fail counter with same TTL window
        String fkey = FAIL_PREFIX + phone;
        Long cnt = redis.opsForValue().increment(fkey);
        // align fail TTL with otp TTL if first time
        if (cnt != null && cnt == 1L) {
            Long ttlSec = redis.getExpire(key);
            if (ttlSec != null && ttlSec > 0) {
                redis.expire(fkey, ttlSec, java.util.concurrent.TimeUnit.SECONDS);
            } else {
                redis.expire(fkey, TTL);
            }
        }
        int used = cnt == null ? 1 : cnt.intValue();
        if (used >= MAX_ATTEMPTS) {
            // lock: force re-send by deleting otp
            redis.delete(key);
            res.success = false;
            res.locked = true;
            res.remaining = 0;
        } else {
            res.success = false;
            res.locked = false;
            res.remaining = Math.max(0, MAX_ATTEMPTS - used);
        }
        return res;
    }
}


