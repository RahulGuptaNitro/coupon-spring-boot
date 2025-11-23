package com.test.coupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes; // Import is already present

import java.util.Map;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String type; // "cart-wise", "product-wise", "bxgy"

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> details;

    private boolean isActive = true;

}