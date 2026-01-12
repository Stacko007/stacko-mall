package com.stacko.mall.infra.catalog;

import com.stacko.mall.domain.catalog.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogInfraConfiguration {

    @Bean
    public ProductRepository productRepository() {
        return new InMemoryProductRepository();
    }
}
