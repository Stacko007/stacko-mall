package com.stacko.mall.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.stacko.mall", "com.stacko.user"})
@MapperScan("com.stacko.mall.infra.dao")
public class StackoMallApplication {

    
    public static void main(String[] args) {
        SpringApplication.run(StackoMallApplication.class, args);
    }
}
