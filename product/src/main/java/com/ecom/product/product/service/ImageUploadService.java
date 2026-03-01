package com.ecom.product.product.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageUploadService {
    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadImage(MultipartFile file, Long productId ){

        try {
            Map<String,String> params = new HashMap<>();

            params.put("folder","repository");
            params.put("public_id","prod"+"_"+productId+"_"+System.currentTimeMillis());

            return this.cloudinary
                       .uploader()
                       .upload(file.getBytes(),params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
