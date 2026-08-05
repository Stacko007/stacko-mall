package com.stacko.mall.application.command;

public class UpdateProductCategoryCommand extends CreateProductCategoryCommand {
    private String categoryId;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
