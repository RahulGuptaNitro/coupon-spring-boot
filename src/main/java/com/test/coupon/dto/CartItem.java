package com.test.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {

    @JsonProperty("product_id")
    private Long productId;

    private int quantity;

    private BigDecimal price;

    @JsonProperty("total_discount")
    private BigDecimal totalDiscount; // To be filled by response

}
