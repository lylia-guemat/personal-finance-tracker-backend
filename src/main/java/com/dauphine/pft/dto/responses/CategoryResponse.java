package com.dauphine.pft.dto.responses;

import com.dauphine.pft.models.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;
    private String name;
    private String description;
    private CategoryType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
