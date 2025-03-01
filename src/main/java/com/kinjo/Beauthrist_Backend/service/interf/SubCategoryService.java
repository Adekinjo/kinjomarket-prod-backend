package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.SubCategoryDto;

public interface SubCategoryService {

    Response createSubCategory(SubCategoryDto subCategoryRequest);

    Response updateSubCategory(Long subCategoryId, SubCategoryDto subCategoryRequest);

    Response deleteSubCategory(Long subCategoryId);

    Response getSubCategoryById(Long subCategoryId);
    Response searchSubCategories(String query);

}