package com.cdp.ecosaas.procurement.supplier.infrastructure.config;

import com.cdp.ecosaas.procurement.supplier.domain.service.CertExpiryDomainService;
import com.cdp.ecosaas.procurement.supplier.domain.service.ChangeReviewService;
import com.cdp.ecosaas.procurement.supplier.domain.service.ContactDomainService;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierCodeGenerator;
import com.cdp.ecosaas.procurement.supplier.domain.service.SupplierLifecycleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 供应商领域服务 Spring Bean 注册配置。
 * <p>
 * 领域服务不依赖 Spring 框架（与 auth 模块 {@code DomainServiceConfig} 一致），
 * 通过此配置类在基础设施层注册为 Bean，供应用层处理器注入。
 */
@Configuration
public class SupplierDomainServiceConfig {

    @Bean
    SupplierLifecycleService supplierLifecycleService() {
        return new SupplierLifecycleService();
    }

    @Bean
    ChangeReviewService changeReviewService() {
        return new ChangeReviewService();
    }

    @Bean
    ContactDomainService contactDomainService() {
        return new ContactDomainService();
    }

    @Bean
    CertExpiryDomainService certExpiryDomainService() {
        return new CertExpiryDomainService();
    }

    @Bean
    SupplierCodeGenerator supplierCodeGenerator() {
        return new SupplierCodeGenerator();
    }
}
