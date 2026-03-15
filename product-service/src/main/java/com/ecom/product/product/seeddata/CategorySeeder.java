package com.ecom.product.product.seeddata;

import com.ecom.product.product.model.Category;
import com.ecom.product.product.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {

        if(categoryRepository.count() > 0) return;

        List<Category> categories = List.of(
                new Category("Shoes","Footwear including sports and casual shoes"),
                new Category("Mobiles","Smartphones and mobile accessories"),
                new Category("Laptops","Personal and gaming laptops"),
                new Category("Clothing","Men and women clothing"),
                new Category("Accessories","Watches, headphones and gadgets"),
                new Category("Electronics","Electronic devices and gadgets")
        );

        categoryRepository.saveAll(categories);
    }
}