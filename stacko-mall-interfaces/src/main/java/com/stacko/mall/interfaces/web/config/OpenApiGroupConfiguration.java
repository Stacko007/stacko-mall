package com.stacko.mall.interfaces.web.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupConfiguration {

    @Bean
    public GroupedOpenApi mallCOpenApi() {
        return GroupedOpenApi.builder()
                .group("mall-c")
                .pathsToMatch("/api/c/**")
                .build();
    }

    @Bean
    public GroupedOpenApi mallAdminOpenApi() {
        return GroupedOpenApi.builder()
                .group("mall-admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
