package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.ProductDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<ProductDto> searchProducts(String query);

    Response createProduct( Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock);

    Response updateProduct(Long productId, Long subCategory,  List<MultipartFile> images, String name, String description, BigDecimal price, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock);

    Response deleteProduct(Long productId);

    Response getProductById(Long productId);

    Response getAllProduct();

    List<ProductDto> getRelatedProducts(String query);

    Response getProductByCategory(Long categoryId);

    Response searchProduct(String searchValue, Long userId, Long categoryId);

    Response getProductSuggestions(String query);

    Response getProductsByNameAndCategory(String name, Long categoryId);

    Response searchProductsWithPrice(String query, Long categoryId, Long userId);

    List<Product> searchProductsBySubCategory(Long subCategoryId);

    Response getAllProductBySubCategory(Long subCategoryId);

    Response getAllProductsWithLikes();

    Response getTrendingProducts();
    void trackProductView(Long productId);

    Response likeProduct(Long productId);

    Response createProductForCompany(Long companyId, Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock);

    Response updateProductForCompany(Long productId, Long companyId, Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock);

    Response deleteProductForCompany(Long productId, Long companyId);

    Response getAllProductsByUser(Long userId);

    Response getSearchSuggestions(String query);

}

