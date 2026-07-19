package com.rit.spms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves uploaded organization logos (see OrganizationProvisioningService) back out as
 * static files. Local filesystem for now -- moving to object storage later only changes
 * what Organization.logoPath resolves to, not this mapping's shape. */
@Configuration
public class UploadsWebMvcConfig implements WebMvcConfigurer {

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadsDir.endsWith("/") ? uploadsDir : uploadsDir + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + location);
    }
}
