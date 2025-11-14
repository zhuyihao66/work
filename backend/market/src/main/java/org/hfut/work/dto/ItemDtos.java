package org.hfut.work.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ItemDtos {
    @Data
    public static class CreateRequest {
        @NotBlank
        private String title;
        private String description;
        @NotNull
        @Min(0)
        private BigDecimal price;
        private String currency = "CNY";
        private String condition; // new/like_new/good/fair/poor
        private Integer quantity = 1;
        private Long categoryId;
        private String location;
        private List<String> images; // url list
    }
}


