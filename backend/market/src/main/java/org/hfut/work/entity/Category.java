package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("categories")
public class Category {
    private Long id;
    private String name;
    @TableField("parent_id")
    private Long parentId;
    private String slug;
    @TableField("created_at")
    private LocalDateTime createdAt;
}


