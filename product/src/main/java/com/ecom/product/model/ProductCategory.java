package com.ecom.product.model;

import jakarta.persistence.*;

@Entity
@Table(name = "product_category" ,uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id","category_id"})})
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    public ProductCategory(Product product, Category category) {
        this.product = product;
        this.category = category;
    }

    protected ProductCategory() {
    }

    public Category getCategory() {
        return category;
    }
}