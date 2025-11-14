package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("item_id")
    private Long itemId;
    @TableField("title_snapshot")
    private String titleSnapshot;
    @TableField("price_snapshot")
    private BigDecimal priceSnapshot;
    private Integer quantity;
}


