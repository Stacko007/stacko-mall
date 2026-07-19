package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.MemberStatus;

import java.time.Instant;
import java.util.Objects;

public class Member {
    private final MemberId id;
    private final String tenantId;
    private final Long stackoUserId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private MemberStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Member(MemberId id,
                   String tenantId,
                   Long stackoUserId,
                   String username,
                   String nickname,
                   String phone,
                   String email,
                   MemberStatus status,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.stackoUserId = Objects.requireNonNull(stackoUserId, "stackoUserId");
        this.username = username;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Member create(String tenantId, Long stackoUserId, String username, String phone, String email) {
        Instant now = Instant.now();
        return new Member(MemberId.newId(), tenantId, stackoUserId, username, username, phone, email, MemberStatus.ACTIVE, now, now);
    }

    public static Member restore(MemberId id,
                                 String tenantId,
                                 Long stackoUserId,
                                 String username,
                                 String nickname,
                                 String phone,
                                 String email,
                                 MemberStatus status,
                                 Instant createdAt,
                                 Instant updatedAt) {
        return new Member(id, tenantId, stackoUserId, username, nickname, phone, email, status, createdAt, updatedAt);
    }

    public void syncProfile(String username, String phone, String email) {
        this.username = username;
        this.phone = phone;
        this.email = email;
        if (nickname == null || nickname.isBlank()) {
            this.nickname = username;
        }
        this.updatedAt = Instant.now();
    }

    public void ensureActive() {
        if (MemberStatus.DISABLED.equals(status)) {
            throw new IllegalStateException("Member disabled");
        }
    }

    public MemberId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Long getStackoUserId() {
        return stackoUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
