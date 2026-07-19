package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.MemberId;
import com.stacko.mall.domain.repository.MemberRepository;
import com.stacko.mall.infra.dao.MemberMapper;
import com.stacko.mall.infra.po.MemberEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisMemberRepository implements MemberRepository {
    private final MemberMapper memberMapper;

    public MybatisMemberRepository(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = MemberEntity.fromDomain(member);
        MemberEntity existing = memberMapper.selectById(entity.getId());
        if (existing == null) {
            memberMapper.insert(entity);
            return member;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Member tenant mismatch");
        }
        memberMapper.updateById(entity);
        return member;
    }

    @Override
    public Optional<Member> findById(String tenantId, MemberId id) {
        LambdaQueryWrapper<MemberEntity> query = new LambdaQueryWrapper<>();
        query.eq(MemberEntity::getTenantId, tenantId)
                .eq(MemberEntity::getId, id.value());
        return Optional.ofNullable(memberMapper.selectOne(query))
                .map(MemberEntity::toDomain);
    }

    @Override
    public Optional<Member> findByStackoUserId(String tenantId, Long stackoUserId) {
        LambdaQueryWrapper<MemberEntity> query = new LambdaQueryWrapper<>();
        query.eq(MemberEntity::getTenantId, tenantId)
                .eq(MemberEntity::getStackoUserId, stackoUserId);
        return Optional.ofNullable(memberMapper.selectOne(query))
                .map(MemberEntity::toDomain);
    }

    @Override
    public List<Member> listByTenant(String tenantId) {
        LambdaQueryWrapper<MemberEntity> query = new LambdaQueryWrapper<>();
        query.eq(MemberEntity::getTenantId, tenantId)
                .orderByDesc(MemberEntity::getUpdatedAt);
        return memberMapper.selectList(query).stream()
                .map(MemberEntity::toDomain)
                .toList();
    }
}
