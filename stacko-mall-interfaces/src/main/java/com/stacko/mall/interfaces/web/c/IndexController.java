package com.stacko.mall.interfaces.web.c;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/c/index")
@Tag(name = "商城-C端", description = "C端基础接口")
public class IndexController {

    @GetMapping({"", "/"})
    public String index() {
        return "Hello World!";
    }
}
