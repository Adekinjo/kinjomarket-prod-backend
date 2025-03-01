package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.CategoryDto;
import com.kinjo.Beauthrist_Backend.dto.Response;

public interface CategoryService {

    Response createCategory(CategoryDto categoryRequest);

    Response updateCategory(Long categoryId,CategoryDto categoryRequest);

    Response getAllCategory();

    Response getCategoryById(Long categoryId);

    Response deleteCategory(Long categoryId);

    Response getCategoryByIdWithSubCategories(Long categoryId);

    Response searchCategories(String query);

}






