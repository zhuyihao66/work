package org.hfut.work.service;

import org.hfut.work.util.VerifyUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class CaptchaService {
    private static final String PREFIX = "captcha:";
    private static final Duration TTL = Duration.ofMinutes(2);

    @Resource
    private StringRedisTemplate redis;

    public static class Captcha {
        public String id;
        public String imageBase64; // data:image/png;base64,...
    }

    public Captcha generate() {
        VerifyUtil verify = VerifyUtil.newBuilder()
                .setSize(4)
                .setWidth(100)
                .setHeight(40)
                .setFontSize(26)
                .setLines(6)
                .setTilt(true)
                .build();
        Object[] res = verify.createImage();
        String text = String.valueOf(res[0]);
        BufferedImage img = (BufferedImage) res[1];
        String id = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(PREFIX + id, text.toLowerCase(), TTL);
        String base64 = imageToBase64(img);
        Captcha c = new Captcha();
        c.id = id;
        c.imageBase64 = "data:image/png;base64," + base64;
        return c;
    }

    public boolean verify(String id, String answer) {
        if (id == null || answer == null) return false;
        String key = PREFIX + id;
        String val = redis.opsForValue().get(key);
        if (val != null && val.equals(answer.toLowerCase())) {
            redis.delete(key);
            return true;
        }
        return false;
    }

    private String imageToBase64(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Captcha image error");
        }
    }
}


