package org.hfut.work.controller;

import org.hfut.work.common.ApiResponse;
import org.hfut.work.entity.User;
import org.hfut.work.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户公开信息控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Resource
    private UserMapper userMapper;

    /**
     * 获取用户公开信息（用于店铺页面展示）
     */
    @GetMapping("/{userId}")
    public ApiResponse<Map<String, Object>> getUserInfo(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("displayName", user.getDisplayName());
        result.put("avatarUrl", user.getAvatarUrl());
        // 不返回敏感信息如email、phone等
        
        return ApiResponse.ok(result);
    }
}

