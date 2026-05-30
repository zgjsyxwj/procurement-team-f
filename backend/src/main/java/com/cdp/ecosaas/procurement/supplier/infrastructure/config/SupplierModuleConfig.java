package com.cdp.ecosaas.procurement.supplier.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 供应商模块基础设施配置。
 * <p>
 * 集中启用本模块的 {@code @ConfigurationProperties}，并开启定时任务支持
 * （证件到期提醒，Req 12.1，调度时间见 {@code supplier.cert-expiry.cron}）。
 * 邮件发送复用模块 01 的 {@code spring.mail.*} / {@code auth.mail} 配置，无需在此重复声明。
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(OssProperties.class)
public class SupplierModuleConfig {
}
