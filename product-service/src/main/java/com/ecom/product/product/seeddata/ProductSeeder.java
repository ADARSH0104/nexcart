package com.ecom.product.product.seeddata;

import com.ecom.product.product.model.Product;
import com.ecom.product.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//@Component
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        int TOTAL = 150000;
        int BATCH_SIZE = 1000;

        Faker faker = new Faker();
        Random random = new Random();

        List<String> brands = List.of(
                "Nike", "Adidas", "Puma", "Apple", "Samsung",
                "Sony", "LG", "Boat", "Dell", "HP"
        );

        List<String> categories = List.of(
                "Shoes", "Electronics", "Clothing",
                "Accessories", "Mobiles", "Laptops"
        );

        List<Product> batch = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (int i = 1; i <= TOTAL; i++) {

            Product p = new Product();
            p.setName(faker.commerce().productName());
            p.setDescription(faker.lorem().sentence(12));
            p.setBrand(brands.get(random.nextInt(brands.size())));
//            p.setCategory(categories.get(random.nextInt(categories.size())));

            batch.add(p);

            if (batch.size() == BATCH_SIZE) {
                productRepository.saveAll(batch);
                batch.clear();
                System.out.println("Inserted: " + i);
            }
        }

        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
        }

        long end = System.currentTimeMillis();
        System.out.println("Seeding done in ms: " + (end - start));
    }
}