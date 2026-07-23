package com.stacko.mall.application.service;

import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberApplicationService {
    private final MemberRepository memberRepository;

    public MemberApplicationService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member ensureMember(String tenantId, Long accountId, Long membershipId,
                               String username, String phone, String email) {
        Member member = memberRepository.findByMembershipId(tenantId, membershipId)
                .map(existing -> {
                    if (!existing.getAccountId().equals(accountId)) {
                        throw new IllegalStateException("Member account mismatch");
                    }
                    existing.syncProfile(username, phone, email);
                    return existing;
                })
                .orElseGet(() -> Member.create(
                        tenantId, accountId, membershipId, username, phone, email));
        member.ensureActive();
        return memberRepository.save(member);
    }

    public Map<String, String> getBuyerNames(String tenantId) {
        Map<String, String> buyerNames = new HashMap<>();
        for (Member member : memberRepository.listByTenant(tenantId)) {
            String displayName = getDisplayName(member);
            buyerNames.put(member.getId().value(), displayName);
        }
        return buyerNames;
    }

    private String getDisplayName(Member member) {
        if (member.getNickname() != null && !member.getNickname().isBlank()) {
            return member.getNickname();
        }
        if (member.getUsername() != null && !member.getUsername().isBlank()) {
            return member.getUsername();
        }
        return member.getMembershipId().toString();
    }
}
