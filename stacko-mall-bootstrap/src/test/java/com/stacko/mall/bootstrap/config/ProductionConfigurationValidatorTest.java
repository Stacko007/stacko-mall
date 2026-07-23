package com.stacko.mall.bootstrap.config;

import com.stacko.mall.interfaces.web.security.GatewayIdentityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionConfigurationValidatorTest {

    @Test
    void acceptsHardenedProductionSettings() {
        assertDoesNotThrow(() -> ProductionConfigurationValidator.validate(
                environment(), identityProperties()));
    }

    @Test
    void rejectsP6SpyDatasource() {
        MockEnvironment environment = environment()
                .withProperty("spring.datasource.url", "jdbc:p6spy:mysql://db/stacko_mall");

        assertThrows(IllegalStateException.class,
                () -> ProductionConfigurationValidator.validate(environment, identityProperties()));
    }

    @Test
    void rejectsWeakIdentitySigningSecret() {
        GatewayIdentityProperties properties = new GatewayIdentityProperties();
        properties.setSigningSecret("local-only-change-this-identity-secret");

        assertThrows(IllegalStateException.class,
                () -> ProductionConfigurationValidator.validate(environment(), properties));
    }

    @Test
    void rejectsShortCallbackSecret() {
        MockEnvironment environment = environment()
                .withProperty("payment.mock.callback-secret", "short");

        assertThrows(IllegalStateException.class,
                () -> ProductionConfigurationValidator.validate(environment, identityProperties()));
    }

    private MockEnvironment environment() {
        return new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://db/stacko_mall")
                .withProperty("spring.datasource.username", "stacko_mall")
                .withProperty("spring.datasource.password", "strong-db-password")
                .withProperty("spring.cloud.nacos.discovery.enabled", "true")
                .withProperty("spring.cloud.nacos.discovery.server-addr", "nacos:8848")
                .withProperty("spring.cloud.nacos.discovery.username", "stacko")
                .withProperty("spring.cloud.nacos.discovery.password", "strong-nacos-password")
                .withProperty("payment.mock.callback-secret",
                        "strong-payment-callback-secret-1234")
                .withProperty("user.platform.web.swagger-bypass", "false")
                .withProperty("springdoc.api-docs.enabled", "false")
                .withProperty("springdoc.swagger-ui.enabled", "false");
    }

    private GatewayIdentityProperties identityProperties() {
        GatewayIdentityProperties properties = new GatewayIdentityProperties();
        properties.setSigningSecret("strong-identity-signing-secret-1234");
        return properties;
    }
}
