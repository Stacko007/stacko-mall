package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.enums.MemberStatus;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.MemberId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_member")
public class MemberEntity {
    @TableId
    private String id;
    private String tenantId;
    private Long accountId;
    private Long membershipId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberEntity fromDomain(Member member) {
        MemberEntity entity = new MemberEntity();
        entity.setId(member.getId().value());
        entity.setTenantId(member.getTenantId());
        entity.setAccountId(member.getAccountId());
        entity.setMembershipId(member.getMembershipId());
        entity.setUsername(member.getUsername());
        entity.setNickname(member.getNickname());
        entity.setPhone(member.getPhone());
        entity.setEmail(member.getEmail());
        entity.setStatus(member.getStatus());
        entity.setCreatedAt(toLocalDateTime(member.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(member.getUpdatedAt()));
        return entity;
    }

    public Member toDomain() {
        return Member.restore(
                new MemberId(id),
                tenantId,
                accountId,
                membershipId,
                username,
                nickname,
                phone,
                email,
                status,
                toInstant(createdAt),
                toInstant(updatedAt)
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.toInstant(ZoneOffset.UTC);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
