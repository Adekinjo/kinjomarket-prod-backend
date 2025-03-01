package com.kinjo.Beauthrist_Backend.dto;//package com.kinjo.Beauthrist_Backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder  // new
public class CategoryDto {

    private Long id;
    private String name;
    private List<ProductDto> productList;

    private List<SubCategoryDto> subCategories;

}




//import lombok.Data;
//import java.util.List;
//
//@Data
//public class CategoryDto {
//    private Long id;
//    private String name;
//    private List<SubCategoryDto> subCategories;
//}