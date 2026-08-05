package com.stacko.mall.infra.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stacko.mall.infra.po.ShippingAddressEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShippingAddressMapper extends BaseMapper<ShippingAddressEntity> {
}
