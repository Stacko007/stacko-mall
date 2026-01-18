package com.stacko.mall.infra.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stacko.mall.infra.po.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {
}
