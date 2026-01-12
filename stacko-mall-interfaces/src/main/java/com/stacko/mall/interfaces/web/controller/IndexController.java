package com.stacko.mall.interfaces.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/api/index")
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "Hello World!";
    }
}
