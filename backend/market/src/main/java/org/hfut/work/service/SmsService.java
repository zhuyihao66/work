package org.hfut.work.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    public void sendCode(String phone, String code) {
        // TODO: integrate real SMS provider here (Aliyun/Tencent/Twilio)
        log.info("[SMS] send to {} code {}", phone, code);
    }
}


