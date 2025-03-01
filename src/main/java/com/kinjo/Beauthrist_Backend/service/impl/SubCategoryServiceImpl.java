package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.SubCategoryDto;
import com.kinjo.Beauthrist_Backend.entity.Category;
import com.kinjo.Beauthrist_Backend.entity.SubCategory;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.CategoryRepo;
import com.kinjo.Beauthrist_Backend.repository.SubCategoryRepo;
import com.kinjo.Beauthrist_Backend.service.interf.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryRepo subCategoryRepo;
    private final CategoryRepo categoryRepo;
    private final EntityDtoMapper entityDtoMapper;

    @Override
    public Response createSubCategory(SubCategoryDto subCategoryRequest) {
        Category category = categoryRepo.findById(subCategoryRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        SubCategory subCategory = new SubCategory();
        subCategory.setName(subCategoryRequest.getName());
        subCategory.setCategory(category);

        subCategoryRepo.save(subCategory);

        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("SubCategory created successfully")
                .build();
    }

    @Override
    public Response updateSubCategory(Long subCategoryId, SubCategoryDto subCategoryRequest) {
        SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        subCategory.setName(subCategoryRequest.getName());
        subCategoryRepo.save(subCategory);

        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("SubCategory updated successfully")
                .build();
    }

    @Override
    public Response deleteSubCategory(Long subCategoryId) {
        SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        subCategoryRepo.delete(subCategory);

        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("SubCategory deleted successfully")
                .build();
    }

    @Override
    public Response getSubCategoryById(Long subCategoryId) {
        SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        SubCategoryDto subCategoryDto = entityDtoMapper.mapSubCategoryToDto(subCategory);

        return Response.builder()
                .status(200)
                .subCategory(subCategoryDto)
                .build();
    }
    @Override
    public Response searchSubCategories(String query) {
        List<SubCategory> subCategories = subCategoryRepo.findByNameContainingIgnoreCase(query);
        List<SubCategoryDto> subCategoryDtos = subCategories.stream()
                .map(entityDtoMapper::mapSubCategoryToDto)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .subCategoryList(subCategoryDtos)
                .build();
    }
}