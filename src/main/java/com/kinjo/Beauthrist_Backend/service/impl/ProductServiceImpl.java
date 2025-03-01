package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.ProductDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.SearchSuggestionDto;
import com.kinjo.Beauthrist_Backend.entity.*;
import com.kinjo.Beauthrist_Backend.exception.InvalidCredentialException;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.*;
import com.kinjo.Beauthrist_Backend.service.AwsS3Service;
import com.kinjo.Beauthrist_Backend.service.interf.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

//    private final SearchService searchHistoryService;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final SubCategoryRepo subCategoryRepo;
    private final CategoryRepo categoryRepo;
    private final EntityDtoMapper entityDtoMapper;
    private final AwsS3Service awsS3Service;


    @Override
    public Response createProduct(Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock) {
        SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        Category category = subCategory.getCategory();
        if (category == null) {
            throw new NotFoundException("Category not found for the given subcategory");
        }

        Product product = new Product();
        product.setSubCategory(subCategory);
        product.setCategory(category);
        product.setName(name);
        product.setStock(stock);
        product.setDescription(description);
        product.setOldPrice(oldPrice);
        product.setNewPrice(newPrice);

        // Add sizes
        if (sizes != null) {
            sizes.forEach(size -> {
                ProductSize productSize = new ProductSize();
                productSize.setSize(size);
                productSize.setProduct(product);
                product.getSizes().add(productSize);
            });
        }

        // Add colors
        if (colors != null) {
            colors.forEach(color -> {
                ProductColor productColor = new ProductColor();
                productColor.setColor(color);
                productColor.setColorCode(productColor.getColorCode()); // Can be null
                productColor.setProduct(product);
                product.getColors().add(productColor);
            });
        }

        // Save product images
        List<ProductImage> productImages = images.stream()
                .map(image -> {
                    String imageUrl = awsS3Service.saveImageToS3(image); // changed
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imageUrl);
                    productImage.setProduct(product);
                    return productImage;
                })
                .collect(Collectors.toList());

        product.setImages(productImages);
        productRepo.save(product);

        return Response.builder()
                .status(200)
                .timeStamp(LocalDateTime.now())
                .message("Product created successfully")
                .build();
    }

    @Override
    public List<Product> searchProductsBySubCategory(Long subCategoryId) {
        return productRepo.findBySubCategoryId(subCategoryId);
    }

    @Override
    public Response updateProduct(Long productId, Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock) {
        // Fetch the product
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // Update subcategory if provided
        if (subCategoryId != null) {
            SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                    .orElseThrow(() -> new NotFoundException("SubCategory not found"));

            // Fetch the category associated with the new subcategory
            Category category = subCategory.getCategory();
            if (category == null) {
                throw new NotFoundException("Category not found for the given subcategory");
            }

            // Update the product's subcategory and category
            product.setSubCategory(subCategory);
            product.setCategory(category);
        }

        // Update name if provided
        if (name != null) {
            product.setName(name);
        }

        // Update description if provided
        if (description != null) {
            product.setDescription(description);
        }

        // Update oldPrice if provided
        if (oldPrice != null) {
            product.setOldPrice(oldPrice);
        }
        if (newPrice != null) {
            product.setNewPrice(newPrice);
        }

        // Update sizes if provided
        if (sizes != null) {
            // Clear existing sizes
            product.getSizes().clear();

            // Add new sizes
            sizes.forEach(size -> {
                ProductSize productSize = new ProductSize();
                productSize.setSize(size);
                productSize.setProduct(product);
                product.getSizes().add(productSize);
            });
        }

        // Update colors if provided
        if (colors != null) {
            // Clear existing colors
            product.getColors().clear();

            // Add new colors
            colors.forEach(color -> {
                ProductColor productColor = new ProductColor();
                productColor.setColor(color);
                productColor.setProduct(product);
                product.getColors().add(productColor);
            });
        }

        // Update images if provided
        if (images != null && !images.isEmpty()) {
            product.getImages().clear();
            List<ProductImage> productImages = images.stream()
                    .map(image -> {
                        String imageUrl = awsS3Service.saveImageToS3(image);  // changed
                        ProductImage productImage = new ProductImage();
                        productImage.setImageUrl(imageUrl);
                        productImage.setProduct(product);
                        return productImage;
                    })
                    .collect(Collectors.toList());

            product.getImages().addAll(productImages);
        }

        // Save the updated product
        productRepo.save(product);

        return Response.builder()
                .status(200)
                .message("Product updated successfully")
                .build();
    }

    @Override
    public Response deleteProduct(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        productRepo.delete(product);
        return Response.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }

    @Override
    public Response getProductById(Long productId) {
        Optional<Product> productOptional = productRepo.findById(productId);
        if (productOptional.isEmpty()) {
            throw new NotFoundException("Product not found");
        }
        Product product = productOptional.get();
        ProductDto productDto = entityDtoMapper.mapProductToDtoBasic(product);
        return Response.builder()
                .status(200)
                .product(productDto)
                .build();
    }


    @Override
    public Response getAllProduct() {
        List<ProductDto> productList = productRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .productList(productList)
                .build();
    }


    @Override
    public List<ProductDto> getRelatedProducts(String searchTerm) {
        List<Product> products = productRepo.findBySearchTerm(searchTerm);
        if (products.isEmpty()) {
            throw new NotFoundException("No related products found for search term: " + searchTerm);
        }
        return products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
    }

    @Override
    public Response getProductByCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        List<ProductDto> productDtos = category.getSubCategories().stream()
                .flatMap(subCategory -> subCategory.getProducts().stream())
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }

    @Override
    public Response searchProduct(String searchValue, Long userId, Long categoryId) {
        List<Product> products = productRepo.findByNameContainingOrDescriptionContaining(searchValue, searchValue);
        if (products.isEmpty()) {
            throw new NotFoundException("Product not found");
        }
        List<ProductDto> productDtoList = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .productList(productDtoList)
                .build();
    }

    @Override
    public Response getProductSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return Response.builder()
                    .status(200)
                    .productList(List.of())
                    .build();
        }

        List<Product> products = productRepo.findByNameContainingOrDescriptionContaining(query, query);
        if (products.isEmpty()) {
            return Response.builder()
                    .status(200)
                    .productList(List.of())
                    .build();
        }
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }

    @Override
    public List<ProductDto> searchProducts(String query) {
        List<Product> products = productRepo.findByNameContainingIgnoreCaseOrCategoryNameContainingIgnoreCase(query, query);
        return products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
    }


    @Override
    public Response getProductsByNameAndCategory(String name, Long categoryId) {
        List<Product> products = productRepo.findByNameAndCategoryId(name, categoryId);
        if (products.isEmpty()) {
            throw new NotFoundException("No products found with the given name and category");
        }
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }


    @Override
    public Response searchProductsWithPrice(String searchQuery, Long categoryId, Long userId) {
        Map<String, Object> parsedQuery = parseSearchQuery(searchQuery);
        String name = (String) parsedQuery.get("name");
        BigDecimal minPrice = (BigDecimal) parsedQuery.get("minPrice");
        BigDecimal maxPrice = (BigDecimal) parsedQuery.get("maxPrice");
        List<Product> products = productRepo.searchProducts(name, categoryId, minPrice, maxPrice);
        if (products.isEmpty()) {
            throw new NotFoundException("No products found with the given criteria");
        }
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }

    private Map<String, Object> parseSearchQuery(String searchQuery) {
        Map<String, Object> result = new HashMap<>();
        String name = searchQuery.trim(); // Default name is the entire query
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        try {
            // Convert the search query to lowercase for case-insensitive comparison
            String lowerCaseQuery = searchQuery.toLowerCase();

            // Check for price-related keywords in the query
            if (lowerCaseQuery.contains("under")) {
                String[] parts = lowerCaseQuery.split("under");
                if (parts.length > 1) {
                    name = parts[0].trim();
                    maxPrice = parsePrice(parts[1]);
                }
            } else if (lowerCaseQuery.contains("over")) {
                String[] parts = lowerCaseQuery.split("over");
                if (parts.length > 1) {
                    name = parts[0].trim();
                    minPrice = parsePrice(parts[1]);
                }
            } else if (lowerCaseQuery.contains("greater than")) {
                String[] parts = lowerCaseQuery.split("greater than");
                if (parts.length > 1) {
                    name = parts[0].trim();
                    minPrice = parsePrice(parts[1]);
                }
            } else if (lowerCaseQuery.contains("less than")) {
                String[] parts = lowerCaseQuery.split("less than");
                if (parts.length > 1) {
                    name = parts[0].trim();
                    maxPrice = parsePrice(parts[1]);
                }
            } else if (searchQuery.contains("$")) {
                String[] parts = searchQuery.split("\\$");
                if (parts.length > 1) {
                    name = parts[0].trim();
                    maxPrice = parsePrice(parts[1]);
                    minPrice = maxPrice; // Exact price match
                }
            } else if (searchQuery.contains("-")) {
                String[] parts = searchQuery.split("-");
                if (parts.length == 2) {
                    name = parts[0].trim();
                    minPrice = parsePrice(parts[0]);
                    maxPrice = parsePrice(parts[1]);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("error");
        }
        result.put("name", name);
        result.put("minPrice", minPrice);
        result.put("maxPrice", maxPrice);

        return result;
    }

    private BigDecimal parsePrice(String priceString) {
        try {
            String cleanedPrice = priceString.replaceAll("[^0-9.]", "").trim();
            if (cleanedPrice.isEmpty()) {
                return null;
            }
            if (cleanedPrice.chars().filter(ch -> ch == '.').count() > 1) {
                return null;
            }
            return new BigDecimal(cleanedPrice);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Response getAllProductBySubCategory(Long subCategoryId) {
        SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        List<ProductDto> productDtos = subCategory.getProducts().stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }

    @Override
    public Response getTrendingProducts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        Page<Product> productPage = productRepo.findTrendingProducts(
                cutoff,
                PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "viewCount"))
        );
        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .trendingProducts(productDtos)
                .build();
    }

    @Override
    @Transactional
    public void trackProductView(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        product.setViewCount((product.getViewCount() != null ? product.getViewCount() : 0) + 1);
        product.setLastViewedDate(LocalDateTime.now());
        productRepo.save(product);
    }

    // In ProductServiceImpl.java

    @Override
    @Transactional
    public Response likeProduct(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        Integer currentLikes = product.getLikes() != null ? product.getLikes() : 0;
        product.setLikes(currentLikes + 1);
        try {
            productRepo.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product likes: " + e.getMessage(), e);
        }
        return Response.builder()
                .status(200)
                .message("Product liked successfully")
                .build();
    }

    @Override
    public Response getAllProductsWithLikes() {
        List<Product> products = productRepo.findAllWithLikes();
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic) // Assuming you have a mapper method
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .message("Products fetched successfully")
                .productList(productDtos)
                .build();
    }


    private List<ProductDto> mapProducts(List<Product> products) {
        return products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
    }




    @Override
    public Response createProductForCompany(Long companyId, Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock) {
        User company = userRepo.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                .orElseThrow(() -> new NotFoundException("SubCategory not found"));

        Category category = subCategory.getCategory();
        if (category == null) {
            throw new NotFoundException("Category not found for the given subcategory");
        }

        Product product = new Product();
        product.setUser(company);
        product.setSubCategory(subCategory);
        product.setCategory(category);
        product.setName(name);
        product.setStock(stock);
        product.setDescription(description);
        product.setOldPrice(oldPrice);
        product.setNewPrice(newPrice);

        // Add sizes
        if (sizes != null) {
            sizes.forEach(size -> {
                ProductSize productSize = new ProductSize();
                productSize.setSize(size);
                productSize.setProduct(product);
                product.getSizes().add(productSize);
            });
        }

        // Add colors
        if (colors != null) {
            colors.forEach(color -> {
                ProductColor productColor = new ProductColor();
                productColor.setColor(color);
                productColor.setProduct(product);
                product.getColors().add(productColor);
            });
        }

        // Save product images
        List<ProductImage> productImages = images.stream()
                .map(image -> {
                    String imageUrl = awsS3Service.saveImageToS3(image);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imageUrl);
                    productImage.setProduct(product);
                    return productImage;
                })
                .collect(Collectors.toList());
        product.setImages(productImages);

        productRepo.save(product);
        return Response.builder()
                .status(200)
                .message("Product created successfully for company: " + company.getName())
                .build();
    }

    @Override
    public Response updateProductForCompany(Long productId, Long companyId, Long subCategoryId, List<MultipartFile> images, String name, String description, BigDecimal oldPrice, BigDecimal newPrice, List<String> sizes, List<String> colors, Integer stock) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        User company = userRepo.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        if (!product.getUser().getId().equals(company.getId())) {
            throw new InvalidCredentialException("You are not authorized to update this product.");
        }

        if (subCategoryId != null) {
            SubCategory subCategory = subCategoryRepo.findById(subCategoryId)
                    .orElseThrow(() -> new NotFoundException("SubCategory not found"));

            // Fetch the category associated with the new subcategory
            Category category = subCategory.getCategory();
            if (category == null) {
                throw new NotFoundException("Category not found for the given subcategory");
            }

            // Update the product's subcategory and category
            product.setSubCategory(subCategory);
            product.setCategory(category);
        }

        // Update name if provided
        if (name != null) {
            product.setName(name);
        }

        // Update description if provided
        if (description != null) {
            product.setDescription(description);
        }

        // Update oldPrice if provided
        if (oldPrice != null) {
            product.setOldPrice(oldPrice);
        }
        if(stock != null){
            product.setStock(stock);
        }
        if (newPrice != null) {
            product.setNewPrice(newPrice);
        }

        // Update sizes if provided
        if (sizes != null) {
            // Clear existing sizes
            product.getSizes().clear();

            // Add new sizes
            sizes.forEach(size -> {
                ProductSize productSize = new ProductSize();
                productSize.setSize(size);
                productSize.setProduct(product);
                product.getSizes().add(productSize);
            });
        }

        // Update colors if provided
        if (colors != null) {
            // Clear existing colors
            product.getColors().clear();

            // Add new colors
            colors.forEach(color -> {
                ProductColor productColor = new ProductColor();
                productColor.setColor(color);
                productColor.setProduct(product);
                product.getColors().add(productColor);
            });
        }

        // Update images if provided
        if (images != null && !images.isEmpty()) {
            // Clear existing images
            product.getImages().clear();

            // Add new images
            List<ProductImage> productImages = images.stream()
                    .map(image -> {
                        String imageUrl = awsS3Service.saveImageToS3(image);  // changed
                        ProductImage productImage = new ProductImage();
                        productImage.setImageUrl(imageUrl);
                        productImage.setProduct(product);
                        return productImage;
                    })
                    .collect(Collectors.toList());

            product.getImages().addAll(productImages);
        }
        productRepo.save(product);
        return Response.builder()
                .status(200)
                .message("Product updated successfully for company: " + company.getName())
                .build();
    }
    @Override
    public Response getAllProductsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Product> products = productRepo.findByUserId(userId);
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }

    @Override
    public Response deleteProductForCompany(Long productId, Long companyId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        User company = userRepo.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        if (!product.getUser().getId().equals(company.getId())) {
            throw new InvalidCredentialException("You are not authorized to delete this product.");
        }

        productRepo.delete(product);
        return Response.builder()
                .status(200)
                .message("Product deleted successfully for company: " + company.getName())
                .build();
    }

    @Override
    public Response getSearchSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return Response.builder()
                    .status(200)
                    .suggestions(List.of())
                    .build();
        }

        // Get all matching entities
        List<Product> products = productRepo.findByNameContaining(query);
        List<Category> categories = categoryRepo.searchCategories(query);
        List<SubCategory> subCategories = subCategoryRepo.searchSubCategories(query);

        // Map to DTOs
        List<SearchSuggestionDto> suggestions = Stream.of(
                products.stream().map(p -> SearchSuggestionDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .type("product")
                        .imageUrl(!p.getImages().isEmpty() ? p.getImages().get(0).getImageUrl() : null)
                        .build()),
                categories.stream().map(c -> SearchSuggestionDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .type("category")
                        .build()),
                subCategories.stream().map(s -> SearchSuggestionDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .type("subcategory")
                        .parentCategory(s.getCategory().getName())
                        .build())
        ).flatMap(s -> s).collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .suggestions(suggestions)
                .build();
    }


}
