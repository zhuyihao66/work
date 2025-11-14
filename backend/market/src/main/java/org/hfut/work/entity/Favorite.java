package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorites")
public class Favorite {
    @TableField("user_id")
    private Long userId;
    
    @TableField("item_id")
    private Long itemId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}

