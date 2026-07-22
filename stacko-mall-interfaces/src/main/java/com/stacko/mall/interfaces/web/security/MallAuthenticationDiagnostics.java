package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MallAuthenticationDiagnostics implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(MallAuthenticationDiagnostics.class);

    private final SaTokenConfig saTokenConfig;
    private final SaTokenDao saTokenDao;

    public MallAuthenticationDiagnostics(SaTokenConfig saTokenConfig,
                                         SaTokenDao saTokenDao) {
        this.saTokenConfig = saTokenConfig;
        this.saTokenDao = saTokenDao;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateLocalAuthentication(saTokenConfig, saTokenDao);
        log.info("Mall authentication architecture: identity=SA_TOKEN, permission=LOCAL, sessionStore={}",
                saTokenDao.getClass().getSimpleName());
    }

    static void validateLocalAuthentication(SaTokenConfig config, SaTokenDao dao) {
        if (dao instanceof SaTokenDaoDefaultImpl) {
            throw new IllegalStateException("Mall Sa-Token authentication requires Redis DAO");
        }
        if (!"Authorization".equalsIgnoreCase(config.getTokenName())
                || !"Bearer".equalsIgnoreCase(config.getTokenPrefix())) {
            throw new IllegalStateException("Mall token transport must use Authorization: Bearer");
        }
    }
}
