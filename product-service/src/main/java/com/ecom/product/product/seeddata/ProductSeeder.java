package com.ecom.product.product.seeddata;

import com.ecom.product.product.model.Category;
import com.ecom.product.product.model.Product;
import com.ecom.product.product.model.ProductImage;
import com.ecom.product.product.repository.CategoryRepository;
import com.ecom.product.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

//@Component
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductSeeder(ProductRepository productRepository,
                         CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    private static final int TOTAL = 150000;
    private static final int BATCH = 1000;

    Random random = new Random();

    List<String> brands = List.of(
            "Nike","Adidas","Apple","Samsung",
            "Sony","Dell","HP","Puma","Boat","LG"
    );

    List<String> productNames = List.of(
            "Pro Max","Ultra","Series X",
            "Elite Edition","Air","Prime",
            "Sport Edition","Gaming Pro"
    );

    List<String> imageSources = List.of(
            "https://source.unsplash.com/600x600/?shoe",
            "https://source.unsplash.com/600x600/?smartphone",
            "https://source.unsplash.com/600x600/?laptop",
            "https://source.unsplash.com/600x600/?headphones",
            "https://source.unsplash.com/600x600/?clothing"
    );

    @Override
    public void run(String... args) {

        List<Category> categories = categoryRepository.findAll();

        List<Product> batch = new ArrayList<>();

        for(int i=1;i<=TOTAL;i++){

            String brand = brands.get(random.nextInt(brands.size()));
            String model = productNames.get(random.nextInt(productNames.size()));

            Product p = new Product(
                    brand + " " + model,
                    "High quality " + brand + " product designed for durability and performance.",
                    brand
            );

            int price = 500 + random.nextInt(50000);

            p.setThumbnailUrl(imageSources.get(random.nextInt(imageSources.size())));
            p.setInStock(true);

            Set<Category> selectedCategories = new HashSet<>();

            int categoryCount = 1 + random.nextInt(3);

            while(selectedCategories.size() < categoryCount){
                selectedCategories.add(
                        categories.get(random.nextInt(categories.size()))
                );
            }

            for(Category c : selectedCategories){
                p.addCategory(c);
            }

            int imageCount = 1 + random.nextInt(3);

            for(int k=0;k<imageCount;k++){
                p.addImage(new ProductImage(p,
                        imageSources.get(random.nextInt(imageSources.size()))));
            }

            batch.add(p);

            if(batch.size()==BATCH){
                productRepository.saveAll(batch);
                batch.clear();
                System.out.println("Inserted "+i);
            }
        }

        if(!batch.isEmpty()){
            productRepository.saveAll(batch);
        }

        System.out.println("Product seeding finished");
    }
}