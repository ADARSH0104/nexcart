package com.ecom.product.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column
    private Instant updatedAt;

    @Column
    private Instant createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategory> productCategories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> productImageSet;

    public Product() {
    }

    public Product(String name, String description) {
        this.name = name;
        this.description = description;
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

    @PrePersist
    public void onCreate(){this.createdAt=Instant.now();}
    @PreUpdate
    public void onUpdate(){
        this.updatedAt = Instant.now();
    }

    public Set<ProductImage> getProductImageSet() {
        return productImageSet;
    }

    public Set<ProductCategory> getProductCategories() {
        return productCategories;
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
