package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("items")
public class Item {
    private Long id;
    @TableField("seller_id")
    private Long sellerId;
    @TableField("category_id")
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private String status; // listed/reserved/sold/archived
    @TableField("item_condition")
    private String itemCondition;
    private Integer quantity;
    @TableField("location_text")
    private String locationText;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}


