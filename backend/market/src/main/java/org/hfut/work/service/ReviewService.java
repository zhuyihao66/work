package org.hfut.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.Review;
import org.hfut.work.entity.Order;
import org.hfut.work.entity.OrderItem;
import org.hfut.work.mapper.ReviewMapper;
import org.hfut.work.mapper.OrderMapper;
import org.hfut.work.mapper.OrderItemMapper;
import org.hfut.work.mapper.UserMapper;
import org.hfut.work.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    @Resource
    private ReviewMapper reviewMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private OrderItemMapper orderItemMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Transactional
    public Long createReview(Long reviewerId, Long orderId, Integer rating, String comment) {
        // 验证订单是否存在且属于该用户
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        
        // 检查订单状态，只有已完成的订单才能评价
        if (!"completed".equals(order.getStatus())) {
            throw new IllegalStateException("只有已完成的订单才能评价");
        }
        
        // 检查是否已经评价过
        Review existing = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getReviewerId, reviewerId));
        if (existing != null) {
            throw new IllegalStateException("该订单已评价");
        }
        
        // 验证评分范围
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        
        // 验证评价人必须是买家
        if (!order.getBuyerId().equals(reviewerId)) {
            throw new IllegalArgumentException("只有买家可以评价");
        }
        
        // 创建评价
        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(order.getSellerId()); // 被评价人是卖家
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        
        return review.getId();
    }
    
    /**
     * 获取商品的所有评论（通过订单关联）
     */
    public List<Map<String, Object>> getReviewsByItemId(Long itemId) {
        // 找到包含该商品的所有订单项
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getItemId, itemId));
        
        if (orderItems.isEmpty()) {
            return List.of();
        }
        
        // 获取这些订单项的订单ID列表
        List<Long> orderIds = orderItems.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查找这些订单的所有评价
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .in(Review::getOrderId, orderIds)
                        .orderByDesc(Review::getCreatedAt));
        
        // 获取评价人信息并构建返回数据
        return reviews.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("rating", r.getRating());
            m.put("comment", r.getComment());
            m.put("createdAt", r.getCreatedAt());
            // 获取评价人信息
            User reviewer = userMapper.selectById(r.getReviewerId());
            if (reviewer != null) {
                Map<String, Object> reviewerMap = new HashMap<>();
                reviewerMap.put("id", reviewer.getId());
                reviewerMap.put("displayName", reviewer.getDisplayName());
                reviewerMap.put("avatarUrl", reviewer.getAvatarUrl());
                m.put("reviewer", reviewerMap);
            }
            return m;
        }).collect(Collectors.toList());
    }
}
