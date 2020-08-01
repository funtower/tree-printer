package com.kilobytech.treeprinter;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域请求配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 页面跨域访问 Controller 过滤
     *
     * @return
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        WebMvcConfigurer.super.addCorsMappings(registry);
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("POST", "GET", "DELETE", "PUT")
                .allowedOrigins("*");
    }
}
