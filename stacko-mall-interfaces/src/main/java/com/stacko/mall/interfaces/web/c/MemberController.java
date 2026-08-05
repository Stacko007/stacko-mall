package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.interfaces.web.ApiResponse;
import com.stacko.mall.interfaces.web.security.CurrentUser;
import com.stacko.mall.interfaces.web.security.CurrentUserContext;
import com.stacko.mall.interfaces.web.view.MemberResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/c/members")
@Tag(name = "商城-C端", description = "会员接口")
public class MemberController {
    private final MemberApplicationService memberApplicationService;
    private final CurrentUserContext currentUserContext;

    public MemberController(MemberApplicationService memberApplicationService,
                            CurrentUserContext currentUserContext) {
        this.memberApplicationService = memberApplicationService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> me(@RequestHeader("X-Tenant-ID") String tenantId) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = memberApplicationService.ensureMember(
                tenantId,
                currentUser.getAccountId(),
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getPhone(),
                currentUser.getEmail()
        );
        return ApiResponse.ok(MemberResponse.from(member));
    }
}
