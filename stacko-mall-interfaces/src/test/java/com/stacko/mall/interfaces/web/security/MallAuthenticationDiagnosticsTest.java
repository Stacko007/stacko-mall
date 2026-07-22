package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MallAuthenticationDiagnosticsTest {

    @Test
    void rejectsInMemoryDao() {
        assertThrows(IllegalStateException.class,
                () -> MallAuthenticationDiagnostics.validateLocalAuthentication(
                        config(), new SaTokenDaoDefaultImpl()));
    }

    @Test
    void acceptsRedisDaoAndBearerTransport() {
        assertDoesNotThrow(() -> MallAuthenticationDiagnostics.validateLocalAuthentication(
                config(), redisDao()));
    }

    private SaTokenConfig config() {
        return new SaTokenConfig()
                .setTokenName("Authorization")
                .setTokenPrefix("Bearer");
    }

    private SaTokenDao redisDao() {
        return (SaTokenDao) Proxy.newProxyInstance(
                SaTokenDao.class.getClassLoader(),
                new Class<?>[]{SaTokenDao.class},
                (proxy, method, args) -> null);
    }
}
