package com.dauphine.pft.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAmountResponse {

    private UUID categoryId;
    private String categoryName;
    private BigDecimal totalAmount;
    private BigDecimal percentage;
    private long transactionCount;
}
