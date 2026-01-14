package com.stacko.mall.infra.catalog;

import com.stacko.mall.domain.catalog.ProductRepository;
import com.stacko.mall.infra.catalog.persistence.ProductMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.stacko.mall.infra.catalog.persistence")
public class CatalogInfraConfiguration {

    @Bean
    public ProductRepository productRepository(ProductMapper productMapper) {
        return new MybatisProductRepository(productMapper);
    }
}
