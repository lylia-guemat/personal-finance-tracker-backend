package com.dauphine.pft.mappers;

import com.dauphine.pft.dto.responses.TransactionResponse;
import com.dauphine.pft.models.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final CategoryMapper categoryMapper;

    // Entity → Response DTO
    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTitle(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDate(),
                categoryMapper.toResponse(transaction.getCategory()),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
