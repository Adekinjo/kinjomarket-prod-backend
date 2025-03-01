package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.ProductDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.exception.InvalidCredentialException;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.repository.UserRepo;
import com.kinjo.Beauthrist_Backend.service.interf.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {


    private final ProductService productService;
    private final UserRepo userRepo;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> createProduct(
            //@RequestParam("categoryId") Long categoryId,
            @RequestParam("subCategoryId") Long subCategoryId,
            @RequestParam("name") String name,
            @RequestParam("stock") Integer stock,
            @RequestParam("description") String description,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam("newPrice") BigDecimal newPrice,
            @RequestParam("images") List<MultipartFile> images) {

        try {
            Response response = productService.createProduct(subCategoryId, images, name, description, oldPrice, newPrice, sizes, colors, stock);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Response.builder()
                            .status(500)
                            .message("Failed to create product: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateProduct(
            @PathVariable Long productId,
            //@RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestPart(required = false) List<MultipartFile> images, // Use @RequestPart for files
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal oldPrice,
            @RequestParam(required = false) BigDecimal newPrice) {
        try {
            Response response = productService.updateProduct(productId, subCategoryId, images,
                    name, description, oldPrice, newPrice, sizes, colors, stock);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Response.builder()
                            .status(500)
                            .message("Failed to update product: " + e.getMessage())
                            .build()
            );
        }
    }


    @DeleteMapping("/delete/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> deleteProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

    @GetMapping("/get-product-by/{productId}")
    public ResponseEntity<Response> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }


    @GetMapping("/get-all")
    public ResponseEntity<Response> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/get-by-category-id/{categoryId}")
    public ResponseEntity<Response> getProductByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchForProduct(@RequestParam String searchValue, Long userId, Long categoryId) {

        return ResponseEntity.ok(productService.searchProduct(searchValue, userId, categoryId));
    }

@GetMapping("/suggestions")
public ResponseEntity<Response> getProductSuggestions(@RequestParam("query") String query) {
    return ResponseEntity.ok(productService.getSearchSuggestions(query));
}

    @GetMapping("/filter-by-name-and-category")
    public ResponseEntity<Response> getProductsByNameAndCategory(
            @RequestParam String name,
            @RequestParam Long categoryId) {
        try {
            Response response = productService.getProductsByNameAndCategory(name, categoryId);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Response.builder()
                            .status(500)
                            .message("Failed to fetch products: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/search-by-subcategory")
    public ResponseEntity<List<Product>> searchProductsBySubCategory(
            @RequestParam(required = false) Long subCategoryId) {
        List<Product> products = productService.searchProductsBySubCategory(subCategoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<Response> getAllProductsBySubCategory(@PathVariable Long subCategoryId) {
        Response response = productService.getAllProductBySubCategory(subCategoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-with-price")
    public ResponseEntity<Response> searchProductsWithPrice(
            @RequestParam String name,
            @RequestParam(required = false) Long userId, // Make userId optional
            @RequestParam(required = false) Long categoryId) {

        try {
            // Call the service method to search products
            Response response = productService.searchProductsWithPrice(name, categoryId, userId);

            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {

            // Handle the case where no products are found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {

            // Handle any other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Response.builder()
                            .status(500)
                            .message("Failed to fetch products: " + e.getMessage())
                            .build()
            );
        }
    }


    @GetMapping("/trending")
    public ResponseEntity<Response> getTrendingProducts() {
        return ResponseEntity.ok(productService.getTrendingProducts());
    }


    @PostMapping("/{productId}/view")
    public ResponseEntity<Void> trackProductView(@PathVariable Long productId) {
        productService.trackProductView(productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/like")
    public ResponseEntity<Response> likeProduct(@PathVariable Long productId) {
        Response response = productService.likeProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-with-likes")
    public ResponseEntity<Response> getAllProductsWithLikes() {
        return ResponseEntity.ok(productService.getAllProductsWithLikes());
    }

    // ProductController.java

    @GetMapping("/get-all-by-user") // No {userId} in path
    @PreAuthorize("hasAnyRole('ROLE_COMPANY', 'ROLE_ADMIN')")
    public ResponseEntity<Response> getAllProductsByUser() {
        // Get userId from JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(productService.getAllProductsByUser(user.getId()));
    }


//    @GetMapping("/get-all-by-user")
//    @PreAuthorize("hasAnyRole('ROLE_COMPANY', 'ROLE_ADMIN')")
//    public ResponseEntity<Response> getAllProductsByUser(@PathVariable Long userId) {
//        if (userId == null) {
//            return ResponseEntity.badRequest().body(
//                    Response.builder()
//                            .status(400)
//                            .message("User ID is required")
//                            .build()
//            );
//        }
//        return ResponseEntity.ok(productService.getAllProductsByUser(userId));
//    }

    // Add these endpoints in ProductController.java

    @PostMapping(value = "/company/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_COMPANY')")
    public ResponseEntity<Response> createCompanyProduct(
            @RequestParam("subCategoryId") Long subCategoryId,
            @RequestParam("name") String name,
            @RequestParam("stock") Integer stock,
            @RequestParam("description") String description,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("newPrice") BigDecimal newPrice,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam("images") List<MultipartFile> images) {

        try {
            // Get authenticated company
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User company = userRepo.findByEmail(authentication.getName())
                    .orElseThrow(() -> new NotFoundException("Company not found"));

            Response response = productService.createProductForCompany(
                    company.getId(),
                    subCategoryId,
                    images,
                    name,
                    description,
                    oldPrice,
                    newPrice,
                    sizes,
                    colors,
                    stock
            );
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/company/update/{productId}")
    @PreAuthorize("hasAuthority('ROLE_COMPANY')")
    public ResponseEntity<Response> updateCompanyProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestPart(required = false) List<MultipartFile> images,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal oldPrice,
            @RequestParam(required = false) BigDecimal newPrice,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) List<String> colors) {

        try {
            // Get authenticated company
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User company = userRepo.findByEmail(authentication.getName())
                    .orElseThrow(() -> new NotFoundException("Company not found"));

            Response response = productService.updateProductForCompany(
                    productId,
                    company.getId(),
                    subCategoryId,
                    images,
                    name,
                    description,
                    oldPrice,
                    newPrice,
                    sizes,
                    colors,
                    stock
            );
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Response.builder()
                            .status(403)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @DeleteMapping("/company/delete/{productId}")
    @PreAuthorize("hasAuthority('ROLE_COMPANY')")
    public ResponseEntity<Response> deleteCompanyProduct(@PathVariable Long productId) {
        try {
            // Get authenticated company
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User company = userRepo.findByEmail(authentication.getName())
                    .orElseThrow(() -> new NotFoundException("Company not found"));

            Response response = productService.deleteProductForCompany(productId, company.getId());
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Response.builder()
                            .status(403)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
