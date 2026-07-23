package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.service.AfterSalesApplicationService;
import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.application.command.ApplyAfterSalesCommand;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.interfaces.web.dto.AfterSalesApplyRequest;
import com.stacko.mall.interfaces.web.security.CurrentUser;
import com.stacko.mall.interfaces.web.security.CurrentUserContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AfterSalesControllerSecurityTest {

    @Test
    void rejectsAfterSalesForAnotherBuyersOrder() {
        AtomicBoolean applied = new AtomicBoolean();
        AfterSalesApplicationService afterSalesService = new AfterSalesApplicationService(null, null, null) {
            @Override
            public AfterSales apply(ApplyAfterSalesCommand command) {
                applied.set(true);
                return null;
            }
        };
        OrderApplicationService orderService = new OrderApplicationService(null, null, null, null) {
            @Override
            public Order get(String tenantId, String orderId) {
                return Order.create(tenantId, "another-member", List.of());
            }
        };
        MemberApplicationService memberService = new MemberApplicationService(null) {
            @Override
            public Member ensureMember(String tenantId, Long accountId, Long membershipId,
                                       String username, String phone, String email) {
                return Member.create(tenantId, accountId, membershipId, username, phone, email);
            }
        };
        CurrentUserContext currentUserContext = new CurrentUserContext() {
            @Override
            public CurrentUser require(String tenantId) {
                return new CurrentUser(3L, 7L, "alice", tenantId);
            }
        };
        AfterSalesController controller = new AfterSalesController(
                afterSalesService, orderService, memberService, currentUserContext);

        AfterSalesApplyRequest request = new AfterSalesApplyRequest();
        request.setOrderId("order-a");

        assertThrows(SecurityException.class, () -> controller.apply("tenant-a", request));
        assertFalse(applied.get());
    }
}
