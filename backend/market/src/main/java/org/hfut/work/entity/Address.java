package org.hfut.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("addresses")
public class Address {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("receiver_name")
    private String receiverName;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    @TableField("address_line")
    private String addressLine;
    @TableField("postal_code")
    private String postalCode;
    @TableField("is_default")
    private Integer isDefault;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}


