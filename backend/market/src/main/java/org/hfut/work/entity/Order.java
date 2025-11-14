package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("order_no")
    private String orderNo;
    @TableField("buyer_id")
    private Long buyerId;
    @TableField("seller_id")
    private Long sellerId;
    private String status; // pending/paid/shipped/completed/cancelled/refunded
    @TableField("total_amount")
    private BigDecimal totalAmount;
    private String currency;
    private String note;
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}


