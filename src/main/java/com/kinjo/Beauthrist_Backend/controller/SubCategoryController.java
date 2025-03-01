package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.SubCategoryDto;
import com.kinjo.Beauthrist_Backend.service.interf.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sub-category")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> createSubCategory(@RequestBody SubCategoryDto subCategoryDto) {
        return ResponseEntity.ok(subCategoryService.createSubCategory(subCategoryDto));
    }

    @PutMapping("/update/{subCategoryId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateSubCategory(@PathVariable Long subCategoryId, @RequestBody SubCategoryDto subCategoryDto) {
        return ResponseEntity.ok(subCategoryService.updateSubCategory(subCategoryId, subCategoryDto));
    }

    @DeleteMapping("/delete/{subCategoryId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> deleteSubCategory(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(subCategoryService.deleteSubCategory(subCategoryId));
    }

    @GetMapping("/get/{subCategoryId}")
    public ResponseEntity<Response> getSubCategoryById(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(subCategoryService.getSubCategoryById(subCategoryId));
    }
    @GetMapping("/search")
    public ResponseEntity<Response> searchSubCategories(@RequestParam String query) {
        return ResponseEntity.ok(subCategoryService.searchSubCategories(query));
    }
}