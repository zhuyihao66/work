package org.hfut.work.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.Order;
import org.hfut.work.mapper.OrderMapper;
import org.hfut.work.service.NotificationService;
import org.hfut.work.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class OrderTimeoutScheduler {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderService orderService;
    @Resource
    private NotificationService notificationService;

    @Scheduled(fixedDelay = 5000)
    public void sweepPendingOrders() {
        List<Order> pending = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, "pending")
                .isNotNull(Order::getExpiresAt));
        LocalDateTime now = LocalDateTime.now();
        for (Order order : pending) {
            long remaining = ChronoUnit.SECONDS.between(now, order.getExpiresAt());
            if (remaining <= 0) {
                orderService.cancelExpiredOrder(order);
            } else if (remaining <= 60) {
                notificationService.notifyUser(order.getBuyerId(), "order_countdown", java.util.Map.of(
                        "orderId", order.getId(),
                        "orderNo", order.getOrderNo(),
                        "remainingSeconds", remaining
                ));
            }
        }
    }
}


