package com.test.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private List<CartItem> items;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("total_discount")
    private BigDecimal totalDiscount;

    @JsonProperty("final_price")
    private BigDecimal finalPrice;

}
