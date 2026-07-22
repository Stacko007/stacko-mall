package com.stacko.mall.interfaces.web.security;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenIdentityConfiguration {
    @Bean
    public StpLogic stackoUserStpLogic() {
        SaStrategy.instance.setHasElement((values, element) -> values != null && values.contains(element));
        return new StpLogic(UserCenterAuthProtocol.LOGIN_TYPE);
    }
}
