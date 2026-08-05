package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.model.Member;

public class MemberResponse {
    private String id;
    private Long accountId;
    private Long membershipId;
    private String username;
    private String nickname;
    private String phone;
    private String email;

    public static MemberResponse from(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId().value());
        response.setAccountId(member.getAccountId());
        response.setMembershipId(member.getMembershipId());
        response.setUsername(member.getUsername());
        response.setNickname(member.getNickname());
        response.setPhone(member.getPhone());
        response.setEmail(member.getEmail());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
