package org.hfut.work.controller;

import org.hfut.work.common.ApiResponse;
import org.hfut.work.service.ItemService;
import org.hfut.work.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.annotation.Resource;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.hfut.work.dto.ItemDtos;

/**
 * @author 19801
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Resource
    private ItemService itemService;
    @Resource
    private CategoryService categoryService;

    @GetMapping
    public ApiResponse<java.util.List<java.util.Map<String,Object>>> list(@RequestParam(defaultValue = "20") int limit,
                                                                          @RequestParam(defaultValue = "0") int offset,
                                                                          @RequestParam(required = false) String q,
                                                                          @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(itemService.listHome(Math.min(limit, 50), Math.max(offset, 0), q, categoryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(itemService.getDetail(id));
    }

    @GetMapping("/nav")
    public ApiResponse<java.util.Map<String, Long>> nav(@RequestParam("currentId") Long currentId) {
        return ApiResponse.ok(itemService.nav(currentId));
    }

    @PostMapping
    public ApiResponse<Long> create(@Validated @RequestBody ItemDtos.CreateRequest req, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        return ApiResponse.ok(itemService.createItem(sellerId, req));
    }

    @GetMapping("/categories")
    public ApiResponse<java.util.List<java.util.Map<String,Object>>> categories() {
        return ApiResponse.ok(categoryService.tree());
    }

    @GetMapping("/seller")
    public ApiResponse<java.util.List<java.util.Map<String,Object>>> sellerList(@RequestParam(defaultValue = "20") int limit,
                                                                                @RequestParam(defaultValue = "0") int offset,
                                                                                @RequestParam(required = false) String status,
                                                                                @RequestParam(required = false) String q,
                                                                                HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        return ApiResponse.ok(itemService.listBySeller(sellerId, Math.min(limit,50), Math.max(offset,0), status, q));
    }

    /**
     * 获取指定卖家的商品列表（公开访问，仅显示上架中的商品）
     */
    @GetMapping("/seller/{sellerId}")
    public ApiResponse<java.util.Map<String,Object>> getSellerItems(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String q) {
        // 只显示上架中的商品
        java.util.List<java.util.Map<String,Object>> items = itemService.listBySeller(sellerId, Math.min(limit,50), Math.max(offset,0), "listed", q);
        long total = itemService.countBySeller(sellerId, "listed", q);
        java.util.Map<String,Object> result = new java.util.HashMap<>();
        result.put("items", items);
        result.put("total", total);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody java.util.Map<String,Object> body, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        itemService.updateItemBasic(sellerId, id, body);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        itemService.deleteItem(sellerId, id);
        return ApiResponse.ok();
    }
}


