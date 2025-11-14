package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shipments")
public class Shipment {
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("shipping_provider")
    private String shippingProvider;
    @TableField("tracking_no")
    private String trackingNo;
    private String status; // pending/shipped/in_transit/delivered/returned/lost
    @TableField("shipped_at")
    private LocalDateTime shippedAt;
    @TableField("delivered_at")
    private LocalDateTime deliveredAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}


