package com.dauphine.pft.dto.responses;

import com.dauphine.pft.models.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopTransactionResponse {

    private UUID id;
    private String title;
    private BigDecimal amount;
    private TransactionType type;
    private String categoryName;
    private LocalDate transactionDate;
}
