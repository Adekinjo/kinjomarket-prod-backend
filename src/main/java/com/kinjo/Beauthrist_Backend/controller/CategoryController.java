package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.CategoryDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.service.interf.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> createCategory(@RequestBody CategoryDto categoryRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Response response = categoryService.createCategory(categoryRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<Response> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    @PutMapping("/update/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryDto categoryRequest) {
        Response response = categoryService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateCategory(@PathVariable Long categoryId){
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
    }

    @GetMapping("/get-category-by-id/{categoryId}")
    public ResponseEntity<Response> getCategoryById(@PathVariable Long categoryId){
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }


    //  for sub category
    @GetMapping("/get-with-subcategories/{categoryId}")
    public ResponseEntity<Response> getCategoryByIdWithSubCategories(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryByIdWithSubCategories(categoryId));
    }
    @GetMapping("/search")
    public ResponseEntity<Response> searchCategories(@RequestParam String query) {
        return ResponseEntity.ok(categoryService.searchCategories(query));
    }
}






