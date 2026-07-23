package com.stacko.mall.bootstrap.config;

import com.stacko.mall.interfaces.web.security.GatewayIdentityProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ProductionConfigurationValidator implements ApplicationRunner {
    private static final Set<String> WEAK_SECRETS = Set.of(
            "123456", "password", "root", "root123", "nacos", "admin",
            "local-only-change-this-identity-secret", "stacko-mall-callback-secret"
    );

    private final Environment environment;
    private final GatewayIdentityProperties identityProperties;

    public ProductionConfigurationValidator(Environment environment,
                                            GatewayIdentityProperties identityProperties) {
        this.environment = environment;
        this.identityProperties = identityProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        validate(environment, identityProperties);
    }

    static void validate(Environment environment, GatewayIdentityProperties identityProperties) {
        String datasourceUrl = required(environment, "spring.datasource.url");
        if (datasourceUrl.contains("p6spy")) {
            throw invalid("production datasource must not use P6Spy");
        }
        required(environment, "spring.datasource.username");
        strongSecret(environment, "spring.datasource.password", 12);
        requireNacos(environment);
        strongValue(identityProperties.getSigningSecret(),
                "stacko.mall.gateway-identity.signing-secret", 32);
        strongSecret(environment, "payment.mock.callback-secret", 32);
        if (environment.getProperty("user.platform.web.swagger-bypass", Boolean.class, false)) {
            throw invalid("Swagger tenant bypass must be disabled");
        }
        if (environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false)
                || environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class, false)) {
            throw invalid("OpenAPI endpoints must be disabled");
        }
    }

    private static void requireNacos(Environment environment) {
        if (!environment.getProperty("spring.cloud.nacos.discovery.enabled", Boolean.class, true)) {
            throw invalid("Nacos discovery must be enabled");
        }
        required(environment, "spring.cloud.nacos.discovery.server-addr");
        required(environment, "spring.cloud.nacos.discovery.username");
        strongSecret(environment, "spring.cloud.nacos.discovery.password", 12);
    }

    private static String required(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw invalid(key + " must be configured");
        }
        return value.trim();
    }

    private static void strongSecret(Environment environment, String key, int minimumLength) {
        strongValue(required(environment, key), key, minimumLength);
    }

    private static void strongValue(String value, String key, int minimumLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() < minimumLength
                || WEAK_SECRETS.contains(normalized.toLowerCase())) {
            throw invalid(key + " uses a weak default");
        }
    }

    private static IllegalStateException invalid(String message) {
        return new IllegalStateException("Invalid production configuration: " + message);
    }
}
