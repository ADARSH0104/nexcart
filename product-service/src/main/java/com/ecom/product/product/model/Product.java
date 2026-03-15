package com.ecom.product.product.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "product",
    indexes={
        @Index(name = "ft_product_search" ,columnList = ("name,description,brand")),
        @Index(name = "ft_product_brand" ,columnList = ("brand"))
    })
public class Product {


    //TODO change product id to uuid
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String brand;


    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    private Boolean inStock;

    @Column
    private Instant updatedAt;

    @Column
    private Instant createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategory> productCategories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> productImageSet;

    @PrePersist
    public void onCreate(){
        this.createdAt=Instant.now();
        this.inStock = false;
    }
    @PreUpdate
    public void onUpdate(){
        this.updatedAt = Instant.now();
    }

    public Product() {
    }

    public Product(String name, String description, String brand) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.productCategories = new HashSet<>();
        this.productImageSet = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Set<ProductImage> getProductImageSet() {
        return productImageSet;
    }

    public Set<ProductCategory> getProductCategories() {
        return productCategories;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public Set<Category> getCategory(){
        return this.getProductCategories().stream().map(ProductCategory::getCategory)
                .collect(Collectors.toSet());
    }
    public void addCategory(Category category) {
        this.productCategories.add(new ProductCategory(this,category));
    }

    public void addImage(ProductImage image) {
        this.productImageSet.add(image);
    }
}
