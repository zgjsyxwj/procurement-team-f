package com.cdp.ecosaas.procurement.supplier.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 供应商模块对象存储（腾讯云 COS）配置。
 * <p>
 * 配置前缀：{@code tencent.oss}，与 Apollo 已发布的连接配置一致（连接参数由 Apollo / K8s Secret 提供，
 * 本地仅占位）。承载证件文件上传/下载所需的 COS 连接参数，以及证件附件上传白名单（Req 10.6）。
 * <ul>
 *   <li>{@code endpoint}：COS 访问域名，如 {@code https://cos.ap-shanghai.myqcloud.com}</li>
 *   <li>{@code cosRegion}：地域，如 {@code ap-shanghai}</li>
 *   <li>{@code bucket}：存储桶名（格式 {@code name-appid}）</li>
 *   <li>{@code accessKeyId} / {@code accessKeySecret}：访问密钥（机密，由 Apollo 覆盖，禁止提交真值）</li>
 *   <li>{@code expireMinutes}：证件下载预签名地址有效期（分钟）</li>
 *   <li>{@code maxFileSize} / {@code allowedExtensions} / {@code allowedContentTypes}：上传白名单（PDF/JPG/PNG，≤100MB）</li>
 * </ul>
 * 注：实际的 COS 客户端与上传/下载逻辑由 {@code OssFileStorageAdapter}（任务 6.1）实现。
 */
@Validated
@ConfigurationProperties(prefix = "tencent.oss")
public record OssProperties(
        @NotBlank String endpoint,
        @NotBlank String cosRegion,
        @NotBlank String bucket,
        @NotBlank String accessKeyId,
        @NotBlank String accessKeySecret,
        @Positive int expireMinutes,
        @NotNull DataSize maxFileSize,
        @NotEmpty List<String> allowedExtensions,
        @NotEmpty List<String> allowedContentTypes
) {
}
