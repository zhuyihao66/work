package org.hfut.work.controller;

import io.jsonwebtoken.Claims;
import org.hfut.work.common.ApiResponse;
import org.hfut.work.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    
    @Resource
    private FavoriteService favoriteService;
    
    // ========== 宝贝收藏 ==========
    
    /**
     * 切换收藏状态（添加/取消）
     */
    @PostMapping("/items/{itemId}")
    public ApiResponse<Map<String, Object>> toggleFavorite(@PathVariable Long itemId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        boolean isFavorited = favoriteService.toggleFavorite(userId, itemId);
        Map<String, Object> result = Map.of(
            "isFavorited", isFavorited,
            "itemId", itemId
        );
        return ApiResponse.ok(result);
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/items/{itemId}/status")
    public ApiResponse<Map<String, Object>> getFavoriteStatus(@PathVariable Long itemId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        boolean isFavorited = favoriteService.isFavorited(userId, itemId);
        Map<String, Object> result = Map.of(
            "isFavorited", isFavorited,
            "itemId", itemId
        );
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取收藏的商品列表
     */
    @GetMapping("/items")
    public ApiResponse<Map<String, Object>> getFavoriteItems(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        var items = favoriteService.getFavoriteItems(userId, Math.min(limit, 50), Math.max(offset, 0));
        long total = favoriteService.getFavoriteCount(userId);
        
        Map<String, Object> result = Map.of(
            "items", items,
            "total", total
        );
        return ApiResponse.ok(result);
    }
    
    // ========== 收藏的店 ==========
    
    /**
     * 切换店铺收藏状态
     */
    @PostMapping("/stores/{sellerId}")
    public ApiResponse<Map<String, Object>> toggleFavoriteStore(@PathVariable Long sellerId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        boolean isFavorited = favoriteService.toggleFavoriteStore(userId, sellerId);
        Map<String, Object> result = Map.of(
            "isFavorited", isFavorited,
            "sellerId", sellerId
        );
        return ApiResponse.ok(result);
    }
    
    /**
     * 检查店铺是否已收藏
     */
    @GetMapping("/stores/{sellerId}/status")
    public ApiResponse<Map<String, Object>> getFavoriteStoreStatus(@PathVariable Long sellerId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        boolean isFavorited = favoriteService.isStoreFavorited(userId, sellerId);
        Map<String, Object> result = Map.of(
            "isFavorited", isFavorited,
            "sellerId", sellerId
        );
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取收藏的店铺列表
     */
    @GetMapping("/stores")
    public ApiResponse<Map<String, Object>> getFavoriteStores(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        var stores = favoriteService.getFavoriteStores(userId, Math.min(limit, 50), Math.max(offset, 0));
        long total = favoriteService.getFavoriteStoreCount(userId);
        
        Map<String, Object> result = Map.of(
            "stores", stores,
            "total", total
        );
        return ApiResponse.ok(result);
    }
    
    // ========== 我的足迹 ==========
    
    /**
     * 记录浏览历史
     */
    @PostMapping("/history/{itemId}")
    public ApiResponse<Void> recordBrowseHistory(@PathVariable Long itemId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        favoriteService.recordBrowseHistory(userId, itemId);
        return ApiResponse.ok();
    }
    
    /**
     * 获取浏览历史
     */
    @GetMapping("/history")
    public ApiResponse<Map<String, Object>> getBrowseHistory(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        var history = favoriteService.getBrowseHistory(userId, Math.min(limit, 50), Math.max(offset, 0));
        long total = favoriteService.getBrowseHistoryCount(userId);
        
        Map<String, Object> result = Map.of(
            "history", history,
            "total", total
        );
        return ApiResponse.ok(result);
    }
    
    /**
     * 删除单个浏览记录
     */
    @DeleteMapping("/history/{itemId}")
    public ApiResponse<Void> deleteBrowseHistory(@PathVariable Long itemId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        favoriteService.deleteBrowseHistory(userId, itemId);
        return ApiResponse.ok();
    }
    
    /**
     * 清空浏览历史
     */
    @DeleteMapping("/history")
    public ApiResponse<Void> clearBrowseHistory(HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("jwtClaims");
        Long userId = ((Number) claims.get("uid")).longValue();
        
        favoriteService.clearBrowseHistory(userId);
        return ApiResponse.ok();
    }
}

