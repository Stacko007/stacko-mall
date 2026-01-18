package com.stacko.mall.interfaces.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/mall/api/index")
public class IndexController {

    @GetMapping({"", "/"})
    public String index() {
        return "Hello World!";
    }
}
