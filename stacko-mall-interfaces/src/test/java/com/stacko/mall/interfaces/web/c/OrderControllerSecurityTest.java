package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.ConfirmReceiptCommand;
import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.application.service.ShippingAddressApplicationService;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.interfaces.web.security.CurrentUser;
import com.stacko.mall.interfaces.web.security.CurrentUserContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderControllerSecurityTest {

    @Test
    void rejectsConfirmReceiptForAnotherBuyersOrder() {
        AtomicBoolean confirmed = new AtomicBoolean();
        OrderApplicationService orderService = new OrderApplicationService(null, null, null, null) {
            @Override
            public Order get(String tenantId, String orderId) {
                Order order = Order.create(tenantId, "another-member", List.of());
                order.pay();
                order.ship("SF", "TRACK-1");
                return order;
            }

            @Override
            public Order confirmReceipt(ConfirmReceiptCommand command) {
                confirmed.set(true);
                return null;
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
                return new CurrentUser(3L, 7L, "alice", tenantId, java.util.Set.of());
            }
        };
        OrderController controller = new OrderController(orderService, memberService, currentUserContext,
                new ShippingAddressApplicationService(null));

        assertThrows(SecurityException.class, () -> controller.confirmReceipt("tenant-a", "confirm-key", "order-a"));
        assertFalse(confirmed.get());
    }
}
