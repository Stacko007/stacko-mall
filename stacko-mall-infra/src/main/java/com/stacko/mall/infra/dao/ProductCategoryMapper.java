package com.stacko.mall.infra.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stacko.mall.infra.po.ProductCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategoryEntity> {
}
