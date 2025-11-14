package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item_images")
public class ItemImage {
    private Long id;
    @TableField("item_id")
    private Long itemId;
    @TableField("image_url")
    private String imageUrl;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_at")
    private LocalDateTime createdAt;
}


