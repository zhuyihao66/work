package org.hfut.work.controller;

import io.jsonwebtoken.Claims;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.common.ApiResponse;
import org.hfut.work.entity.Address;
import org.hfut.work.entity.User;
import org.hfut.work.mapper.AddressMapper;
import org.hfut.work.mapper.UserMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    @Resource
    private UserMapper userMapper;
    @Resource
    private AddressMapper addressMapper;

    @GetMapping
    public ApiResponse<User> me(HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        User u = userMapper.selectById(uid);
        return ApiResponse.ok(u);
    }

    public static class UpdateMeRequest {
        @NotBlank
        public String displayName;
        public String avatarUrl;
        public String phone;
    }

    @PutMapping
    public ApiResponse<Void> updateMe(@Validated @RequestBody UpdateMeRequest body, HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        User u = new User();
        u.setId(uid);
        u.setDisplayName(body.displayName);
        u.setAvatarUrl(body.avatarUrl);
        u.setPhone(body.phone);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return ApiResponse.ok();
    }

    @GetMapping("/addresses")
    public ApiResponse<List<Address>> listAddresses(HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        List<Address> list = addressMapper.selectList(new LambdaQueryWrapper<Address>().eq(Address::getUserId, uid).orderByDesc(Address::getIsDefault).orderByDesc(Address::getUpdatedAt));
        return ApiResponse.ok(list);
    }

    @PostMapping("/addresses")
    public ApiResponse<Long> createAddress(@RequestBody Address a, HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        a.setId(null);
        a.setUserId(uid);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(a);
        return ApiResponse.ok(a.getId());
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<Void> updateAddress(@PathVariable Long id, @RequestBody Address a, HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        a.setId(id);
        a.setUserId(uid);
        a.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(a);
        return ApiResponse.ok();
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id, HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        Address db = addressMapper.selectById(id);
        if (db == null || !db.getUserId().equals(uid)) return ApiResponse.error(404, "地址不存在");
        addressMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @PostMapping("/addresses/{id}/default")
    public ApiResponse<Void> setDefault(@PathVariable Long id, HttpServletRequest req) {
        Claims c = (Claims) req.getAttribute("jwtClaims");
        Long uid = ((Number)c.get("uid")).longValue();
        Address db = addressMapper.selectById(id);
        if (db == null || !db.getUserId().equals(uid)) return ApiResponse.error(404, "地址不存在");
        // clear previous default
        Address clr = new Address();
        clr.setIsDefault(0);
        addressMapper.update(clr, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Address>().eq(Address::getUserId, uid).eq(Address::getIsDefault, 1));
        // set new default
        Address upd = new Address();
        upd.setId(id);
        upd.setIsDefault(1);
        upd.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(upd);
        return ApiResponse.ok();
    }
}


