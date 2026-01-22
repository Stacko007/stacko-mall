package com.stacko.mall.interfaces.web.c;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.stacko.user.contract.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/c/index")
@Tag(name = "商城-C端", description = "C端基础接口")
public class IndexController {

    @GetMapping({"", "/"})
    public ApiResponse<String> index() {
        return ApiResponse.ok("Hello World!");
    }
}
