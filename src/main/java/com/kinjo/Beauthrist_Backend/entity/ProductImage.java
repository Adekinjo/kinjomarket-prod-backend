package com.kinjo.Beauthrist_Backend.entity;

import com.kinjo.Beauthrist_Backend.entity.Product;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // URL of the image stored in AWS S3

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}