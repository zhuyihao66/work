package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("reviewer_id")
    private Long reviewerId;
    @TableField("reviewee_id")
    private Long revieweeId;
    private Integer rating;
    private String comment;
    @TableField("created_at")
    private LocalDateTime createdAt;
}


