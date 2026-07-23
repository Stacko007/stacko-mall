package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.MemberId;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findById(String tenantId, MemberId id);

    Optional<Member> findByMembershipId(String tenantId, Long membershipId);

    List<Member> listByTenant(String tenantId);
}
