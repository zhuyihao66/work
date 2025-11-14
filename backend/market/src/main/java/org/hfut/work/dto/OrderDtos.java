package org.hfut.work.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class OrderDtos {
    @Data
    public static class CreateOrderRequest {
        @NotNull
        private Long itemId;
        @Min(1)
        private Integer quantity = 1;
        private String note;
    }

    @Data
    public static class OrderSummary {
        private Long id;
        private String orderNo;
        private String status;
        private String title;
        private String cover;
        private String currency;
        private String totalAmount;
        private String createdAt;
    }
}


