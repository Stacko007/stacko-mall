package com.stacko.mall.interfaces.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPermissionCoverageTest {
    private static final List<Class<?>> ADMIN_CONTROLLERS = List.of(
            com.stacko.mall.interfaces.web.admin.ProductController.class,
            com.stacko.mall.interfaces.web.admin.ProductCategoryController.class,
            com.stacko.mall.interfaces.web.admin.StockController.class,
            com.stacko.mall.interfaces.web.admin.OrderController.class,
            com.stacko.mall.interfaces.web.admin.PaymentController.class,
            com.stacko.mall.interfaces.web.admin.AfterSalesController.class
    );

    @Test
    void everyAdminEndpointDeclaresPermission() {
        for (Class<?> controller : ADMIN_CONTROLLERS) {
            Arrays.stream(controller.getDeclaredMethods())
                    .filter(this::isEndpoint)
                    .forEach(method -> assertTrue(
                            AnnotatedElementUtils.hasAnnotation(method, RequiresPermission.class),
                            () -> controller.getSimpleName() + "#" + method.getName()
                                    + " must declare @RequiresPermission"));
        }
    }

    private boolean isEndpoint(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class);
    }
}
