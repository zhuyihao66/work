package org.hfut.work.controller;

import io.jsonwebtoken.Claims;
import org.hfut.work.common.ApiResponse;
import org.hfut.work.service.ReviewService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    @Resource
    private ReviewService reviewService;
    
    @PostMapping
    public ApiResponse<Long> create(@RequestBody Map<String, Object> req, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        if (claims == null) {
            return ApiResponse.error(401, "请先登录");
        }
        Long reviewerId = ((Number)claims.get("uid")).longValue();
        
        Long orderId = req.get("orderId") != null ? Long.valueOf(String.valueOf(req.get("orderId"))) : null;
        Integer rating = req.get("rating") != null ? Integer.valueOf(String.valueOf(req.get("rating"))) : null;
        String comment = req.get("comment") != null ? String.valueOf(req.get("comment")) : null;
        
        if (orderId == null || rating == null) {
            return ApiResponse.error(400, "订单ID和评分不能为空");
        }
        
        // 验证评论长度
        if (comment != null && comment.length() > 500) {
            return ApiResponse.error(400, "评论内容不能超过500字符");
        }
        
        try {
            Long reviewId = reviewService.createReview(reviewerId, orderId, rating, comment);
            return ApiResponse.ok(reviewId);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @GetMapping("/item/{itemId}")
    public ApiResponse<java.util.List<Map<String, Object>>> getByItemId(@PathVariable("itemId") Long itemId) {
        return ApiResponse.ok(reviewService.getReviewsByItemId(itemId));
    }
}
