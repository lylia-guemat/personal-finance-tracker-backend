package com.dauphine.pft.controllers;

import com.dauphine.pft.dto.requests.CategoryRequest;
import com.dauphine.pft.dto.responses.CategoryResponse;
import com.dauphine.pft.mappers.CategoryMapper;
import com.dauphine.pft.models.CategoryType;
import com.dauphine.pft.security.SecurityUtils;
import com.dauphine.pft.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories API", description = "Endpoints for managing transaction categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    private UUID CURRENT_USER_ID() {
        return SecurityUtils.getCurrentUserId();
    }
    @GetMapping
    @Operation(summary = "Get all categories",
            description = "Returns all categories for the connected user. " +
                    "Can be filtered by name or type.")
    public ResponseEntity<List<CategoryResponse>> getAll(
            @Parameter(description = "Filter by name (optional)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by type : INCOME, EXPENSE, BOTH (optional)")
            @RequestParam(required = false) CategoryType type) {

        List<CategoryResponse> categories;

        if (name != null && !name.isBlank()) {
            categories = categoryService.searchByName(CURRENT_USER_ID(), name)
                    .stream().map(categoryMapper::toResponse).toList();
        } else if (type != null) {
            categories = categoryService.getAllByUserAndType(CURRENT_USER_ID(), type)
                    .stream().map(categoryMapper::toResponse).toList();
        } else {
            categories = categoryService.getAllByUser(CURRENT_USER_ID())
                    .stream().map(categoryMapper::toResponse).toList();
        }

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "Category UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(
                categoryMapper.toResponse(
                        categoryService.getByIdAndUser(id, CURRENT_USER_ID())));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryMapper.toResponse(
                categoryService.create(
                        CURRENT_USER_ID(),
                        request.getName(),
                        request.getDescription(),
                        request.getType()));
        return ResponseEntity
                .created(URI.create("/v1/categories/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryResponse> update(
            @Parameter(description = "Category UUID") @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(
                categoryMapper.toResponse(
                        categoryService.update(
                                id,
                                CURRENT_USER_ID(),
                                request.getName(),
                                request.getDescription(),
                                request.getType())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category",
            description = "Cannot delete a category that is used by existing transactions")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category UUID") @PathVariable UUID id) {
        categoryService.delete(id, CURRENT_USER_ID());
        return ResponseEntity.noContent().build();
    }
}
