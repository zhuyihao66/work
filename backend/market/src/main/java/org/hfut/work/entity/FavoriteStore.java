package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite_stores")
public class FavoriteStore {
    @TableField("user_id")
    private Long userId;
    
    @TableField("seller_id")
    private Long sellerId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}

