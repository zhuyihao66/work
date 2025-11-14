package org.hfut.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.dto.AuthDtos;
import org.hfut.work.entity.User;
import org.hfut.work.mapper.UserMapper;
import org.hfut.work.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class AuthService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        // Check existing by email
        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (exists != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        if (StringUtils.hasText(req.getPhone())) {
            user.setPhone(req.getPhone());
        }
        user.setDisplayName(req.getDisplayName());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setRole("user");
        user.setStatus("active");
        user.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.setUserId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setDisplayName(user.getDisplayName());
        resp.setRole(user.getRole());
        resp.setToken(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole()));
        return resp;
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (user == null) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("账号不可用");
        }
        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.setUserId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setDisplayName(user.getDisplayName());
        resp.setRole(user.getRole());
        resp.setToken(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole()));
        return resp;
    }

    public AuthDtos.AuthResponse loginOrRegisterByPhone(String phone) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setEmail("phone-" + phone + "@local");
            user.setDisplayName("用户" + phone.substring(Math.max(0, phone.length()-4)));
            user.setAvatarUrl(null);
            user.setRole("user");
            user.setStatus("active");
            user.setPasswordHash(BCrypt.hashpw("", BCrypt.gensalt()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
        AuthDtos.AuthResponse resp = new AuthDtos.AuthResponse();
        resp.setUserId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setDisplayName(user.getDisplayName());
        resp.setRole(user.getRole());
        resp.setToken(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole()));
        return resp;
    }
}


