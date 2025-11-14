package org.hfut.work.controller;

import io.jsonwebtoken.Claims;
import org.hfut.work.common.ApiResponse;
import org.hfut.work.dto.OrderDtos.CreateOrderRequest;
import org.hfut.work.service.OrderService;
import org.hfut.work.entity.Order;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.Shipment;
import org.hfut.work.mapper.ShipmentMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Resource
    private OrderService orderService;
    @javax.annotation.Resource
    private ShipmentMapper shipmentMapper;

    @PostMapping
    public ApiResponse<java.util.Map<String,Object>> create(@Validated @RequestBody CreateOrderRequest req, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long buyerId = ((Number)claims.get("uid")).longValue();
        Order order = orderService.createFixedPriceOrder(buyerId, req.getItemId(), req.getQuantity(), req.getNote());
        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("id", order.getId());
        payload.put("orderNo", order.getOrderNo());
        payload.put("totalAmount", order.getTotalAmount());
        payload.put("currency", order.getCurrency());
        payload.put("sellerId", order.getSellerId());
        payload.put("expiresAt", order.getExpiresAt());
        long remaining = 0;
        if (order.getExpiresAt() != null) {
            remaining = Math.max(0, Duration.between(LocalDateTime.now(), order.getExpiresAt()).getSeconds());
        }
        payload.put("remainingSeconds", remaining);
        return ApiResponse.ok(payload);
    }

    @GetMapping
    public ApiResponse<java.util.List<java.util.Map<String,Object>>> list(@RequestParam(defaultValue = "20") int limit,
                                                                          @RequestParam(defaultValue = "0") int offset,
                                                                          @RequestParam(required = false) String status,
                                                                          @RequestParam(required = false) String q,
                                                                          HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long buyerId = ((Number)claims.get("uid")).longValue();
        return ApiResponse.ok(orderService.listBuyerOrders(buyerId, Math.min(limit, 50), Math.max(offset, 0), status, q));
    }

    @GetMapping("/{id}")
    public ApiResponse<java.util.Map<String,Object>> detail(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long buyerId = ((Number)claims.get("uid")).longValue();
        return ApiResponse.ok(orderService.getBuyerOrderDetail(buyerId, id));
    }

    @GetMapping("/seller")
    public ApiResponse<java.util.List<java.util.Map<String,Object>>> sellerList(@RequestParam(defaultValue = "20") int limit,
                                                                                @RequestParam(defaultValue = "0") int offset,
                                                                                @RequestParam(required = false) String status,
                                                                                @RequestParam(required = false) String q,
                                                                                HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        return ApiResponse.ok(orderService.listSellerOrders(sellerId, Math.min(limit, 50), Math.max(offset, 0), status, q));
    }

    @GetMapping("/seller/{id}")
    public ApiResponse<java.util.Map<String,Object>> sellerDetail(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        return ApiResponse.ok(orderService.getSellerOrderDetail(sellerId, id));
    }

    @GetMapping("/{id}/shipments")
    public ApiResponse<java.util.List<Shipment>> shipments(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long userId = ((Number)claims.get("uid")).longValue();
        // ensure order belongs to buyer or seller
        try {
            orderService.getBuyerOrderDetail(userId, id);
        } catch (IllegalArgumentException e) {
            // if not buyer, try seller
            orderService.getSellerOrderDetail(userId, id);
        }
        java.util.List<Shipment> list = shipmentMapper.selectList(new LambdaQueryWrapper<Shipment>().eq(Shipment::getOrderId, id).orderByAsc(Shipment::getCreatedAt));
        return ApiResponse.ok(list);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long buyerId = ((Number)claims.get("uid")).longValue();
        orderService.cancelByBuyer(buyerId, id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long buyerId = ((Number)claims.get("uid")).longValue();
        orderService.markPaid(buyerId, id);
        return ApiResponse.ok();
    }

    public static class ShipRequest { public String provider; public String trackingNo; }

    @PostMapping("/{id}/ship")
    public ApiResponse<Void> ship(@PathVariable Long id, @RequestBody ShipRequest body, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long sellerId = ((Number)claims.get("uid")).longValue();
        orderService.shipBySeller(sellerId, id, body.provider, body.trackingNo, shipmentMapper);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id, HttpServletRequest httpReq) {
        Claims claims = (Claims) httpReq.getAttribute("jwtClaims");
        Long buyerId = ((Number)claims.get("uid")).longValue();
        orderService.confirmDelivered(buyerId, id);
        return ApiResponse.ok();
    }
}


