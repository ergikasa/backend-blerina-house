package com.blerinahouse.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {


    // CLOUDINARY_URL format: cloudinary://API_KEY:API_SECRET@CLOUD_NAME
    @Bean
    public Cloudinary cloudinary(@Value("${CLOUDINARY_URL}") String cloudinaryUrl) {
        return new Cloudinary(cloudinaryUrl);
    }
}