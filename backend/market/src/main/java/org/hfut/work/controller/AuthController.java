package org.hfut.work.controller;

import org.hfut.work.common.ApiResponse;
import org.hfut.work.dto.AuthDtos.*;
import org.hfut.work.service.AuthService;
import org.hfut.work.service.CaptchaService;
import org.hfut.work.service.OtpService;
import org.hfut.work.service.SmsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 19801
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;
    @Resource
    private CaptchaService captchaService;
    @Resource
    private OtpService otpService;
    @Resource
    private SmsService smsService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Validated @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Validated @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    public static class CaptchaLoginRequest { public String phone; public String captchaId; public String captcha; }

    public static class OtpSendRequest { public String phone; public String captchaId; public String captcha; }
    public static class OtpVerifyRequest { public String phone; public String code; }

    @PostMapping("/captcha/login")
    public ApiResponse<AuthResponse> captchaLogin(@RequestBody CaptchaLoginRequest req) {
        if (req.phone == null || req.captchaId == null || req.captcha == null) {
            return ApiResponse.error(400, "参数错误");
        }
        if (!captchaService.verify(req.captchaId, req.captcha)) {
            return ApiResponse.error(400, "图形验证码错误或过期");
        }
        return ApiResponse.ok(authService.loginOrRegisterByPhone(req.phone));
    }

    @PostMapping("/otp/send")
    public ApiResponse<Void> sendOtp(@RequestBody OtpSendRequest req) {
        if (req.phone == null || !req.phone.matches("^1\\d{10}$") || req.captchaId == null || req.captcha == null) {
            return ApiResponse.error(400, "参数错误");
        }
        if (!captchaService.verify(req.captchaId, req.captcha)) {
            return ApiResponse.error(400, "图形验证码错误或过期");
        }
        String code = otpService.generateAndStore(req.phone);
        smsService.sendCode(req.phone, code);
        return ApiResponse.ok();
    }

    @PostMapping("/otp/verify")
    public ApiResponse<AuthResponse> verifyOtp(@RequestBody OtpVerifyRequest req) {
        if (req.phone == null || !req.phone.matches("^1\\d{10}$") || req.code == null || !req.code.matches("^\\d{6}$")) {
            return ApiResponse.error(400, "参数错误");
        }
        OtpService.VerifyResult vr = otpService.verifyWithLimit(req.phone, req.code);
        if (vr.success) {
            return ApiResponse.ok(authService.loginOrRegisterByPhone(req.phone));
        }
        if (vr.locked) {
            return ApiResponse.error(429, "错误次数过多，请重新获取验证码");
        }
        return ApiResponse.error(400, "验证码错误，剩余可重试次数：" + vr.remaining);
    }

    @GetMapping("/captcha/new")
    public ApiResponse<CaptchaService.Captcha> newCaptcha() {
        return ApiResponse.ok(captchaService.generate());
    }
}


