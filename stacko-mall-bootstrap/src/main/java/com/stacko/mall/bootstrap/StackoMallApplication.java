package com.stacko.mall.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.stacko.mall")
@ConfigurationPropertiesScan(basePackages = "com.stacko.mall")
@EnableDiscoveryClient
@MapperScan("com.stacko.mall.infra.dao")
@EnableScheduling
public class StackoMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(StackoMallApplication.class, args);
    }
}
