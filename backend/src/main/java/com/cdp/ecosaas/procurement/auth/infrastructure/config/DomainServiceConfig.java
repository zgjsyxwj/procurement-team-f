package com.cdp.ecosaas.procurement.auth.infrastructure.config;

import com.cdp.ecosaas.procurement.auth.domain.model.LockoutPolicy;
import com.cdp.ecosaas.procurement.auth.domain.service.LockoutDomainService;
import com.cdp.ecosaas.procurement.auth.domain.service.PasswordDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务 Spring Bean 注册配置。
 * <p>
 * 领域服务不依赖 Spring 框架（参见 backend_spec §3.1），
 * 通过此配置类在基础设施层注册为 Spring Bean，并从配置属性注入策略。
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    LockoutPolicy lockoutPolicy(AuthLockoutProperties props) {
        return new LockoutPolicy(props.maxFailedAttempts(), props.lockDuration());
    }

    @Bean
    PasswordDomainService passwordDomainService(AuthPasswordProperties props) {
        return new PasswordDomainService(
                props.minLength(),
                props.generatedMinLength(),
                props.generatedMaxLength()
        );
    }

    @Bean
    LockoutDomainService lockoutDomainService(LockoutPolicy lockoutPolicy) {
        return new LockoutDomainService(lockoutPolicy);
    }
}
