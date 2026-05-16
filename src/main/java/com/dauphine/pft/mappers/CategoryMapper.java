package com.dauphine.pft.mappers;

import com.dauphine.pft.dto.responses.CategoryResponse;
import com.dauphine.pft.models.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    // Entity → Response DTO
    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getType(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
