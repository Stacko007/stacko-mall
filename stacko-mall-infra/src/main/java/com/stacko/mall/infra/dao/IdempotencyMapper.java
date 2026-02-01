package com.stacko.mall.infra.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stacko.mall.infra.po.IdempotencyEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IdempotencyMapper extends BaseMapper<IdempotencyEntity> {
}
