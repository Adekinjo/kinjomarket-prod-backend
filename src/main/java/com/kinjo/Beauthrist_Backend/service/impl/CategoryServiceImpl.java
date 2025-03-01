package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.CategoryDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.SubCategoryDto;
import com.kinjo.Beauthrist_Backend.entity.Category;
import com.kinjo.Beauthrist_Backend.entity.SubCategory;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.CategoryRepo;
import com.kinjo.Beauthrist_Backend.service.interf.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private  final EntityDtoMapper entityDtoMapper;


    @Override
    @Transactional
    public Response createCategory(CategoryDto categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.getName());


        // Initialize the subCategories list if it's null
        if (category.getSubCategories() == null) {
            category.setSubCategories(new ArrayList<>());
        }

        // Add subcategories if provided
        if (categoryRequest.getSubCategories() != null) {
            for (SubCategoryDto subCategoryDto : categoryRequest.getSubCategories()) {
                SubCategory subCategory = new SubCategory();
                subCategory.setName(subCategoryDto.getName());
                category.addSubCategory(subCategory); // Use the helper method
            }
        }

        // Save the category (cascade will save subcategories)
        categoryRepo.save(category);

        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("Category created successfully")
                .build();
    }


    @Override
    @Transactional
    public Response updateCategory(Long categoryId, CategoryDto categoryRequest) {
        // Fetch the existing category
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Update the category name
        if (categoryRequest.getName() != null) {
            category.setName(categoryRequest.getName());
        }

        // Initialize the subCategories list if it's null
        if (category.getSubCategories() == null) {
            category.setSubCategories(new ArrayList<>());
        }

        // Update subcategories
        if (categoryRequest.getSubCategories() != null) {
            // Create a map of existing subcategories for quick lookup
            Map<Long, SubCategory> existingSubCategories = category.getSubCategories().stream()
                    .collect(Collectors.toMap(SubCategory::getId, subCat -> subCat));

            // Iterate over the subcategories in the request
            for (SubCategoryDto subCategoryDto : categoryRequest.getSubCategories()) {
                if (subCategoryDto.getId() != null) {
                    // Update existing subcategory
                    SubCategory existingSubCategory = existingSubCategories.get(subCategoryDto.getId());
                    if (existingSubCategory != null) {
                        existingSubCategory.setName(subCategoryDto.getName());
                    }
                } else {
                    // Add new subcategory
                    SubCategory newSubCategory = new SubCategory();
                    newSubCategory.setName(subCategoryDto.getName());
                    category.addSubCategory(newSubCategory); // Use the helper method
                }
            }

            // Remove subcategories that are not in the request
            List<Long> requestedSubCategoryIds = categoryRequest.getSubCategories().stream()
                    .map(SubCategoryDto::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            category.getSubCategories().removeIf(subCat -> !requestedSubCategoryIds.contains(subCat.getId()));
        }

        // Save the updated category (cascade will update subcategories)
        categoryRepo.save(category);

        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("Category updated successfully")
                .build();
    }

    @Override
    public Response getAllCategory() {
        List<Category> categories = categoryRepo.findAll();
        List<CategoryDto> categoryDtoList = categories.stream()
                .map(entityDtoMapper::mapCategoryToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .categoryList(categoryDtoList)
                .build();
    }

    @Override
    public Response getCategoryById(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        CategoryDto categoryDto = entityDtoMapper.mapCategoryToDtoBasic(category);
        return Response.builder()
                .status(200)
                .category(categoryDto)
                .build();
    }

    @Override
    public Response deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        categoryRepo.delete(category);
        return Response.builder()
                .status(200)
                .message("Category deleted successfully")
                .build();
    }



    // In CategoryServiceImpl
    @Override
    public Response getCategoryByIdWithSubCategories(Long categoryId) {
        Category category = categoryRepo.findByIdWithSubCategories(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        CategoryDto categoryDto = entityDtoMapper.mapCategoryToDtoBasic(category);

        return Response.builder()
                .status(200)
                .category(categoryDto)
                .build();
    }
    @Override
    public Response searchCategories(String query) {
        List<Category> categories = categoryRepo.findByNameContainingIgnoreCase(query);
        List<CategoryDto> categoryDtos = categories.stream()
                .map(entityDtoMapper::mapCategoryToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .categoryList(categoryDtos)
                .build();
    }

}

