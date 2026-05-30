package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.supplier.domain.port.FileStoragePort;
import com.cdp.ecosaas.procurement.supplier.infrastructure.config.OssProperties;
import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidCertificateFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Locale;

/**
 * 文件存储适配器 —— {@link FileStoragePort} 的腾讯云 COS 实现。
 * <p>
 * 本阶段（任务 6.1）仅实现「证件附件上传白名单校验」（格式 PDF/JPG/PNG、大小 ≤100MB，Req 10.6），
 * 白名单来源于 {@link OssProperties}。真正的 COS 上传/下载客户端尚未接入（沙箱无 COS、bucket 待定），
 * 故 {@link #upload} 校验通过后与 {@link #generateDownloadUrl} 暂抛 {@link UnsupportedOperationException}，
 * 待后续接入腾讯云 COS SDK（{@code com.qcloud:cos_api}）时补齐。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssFileStorageAdapter implements FileStoragePort {

    private static final String COS_NOT_WIRED =
            "腾讯云 COS 客户端尚未接入（任务 6.1 仅实现上传白名单校验，COS 上传/下载延后）";

    private final OssProperties properties;

    @Override
    public String upload(InputStream content, long contentLength, String fileName, String contentType) {
        validateFile(fileName, contentLength, contentType);
        // 白名单校验通过；COS 上传接入延后（见类注释）。
        throw new UnsupportedOperationException(COS_NOT_WIRED);
    }

    @Override
    public String generateDownloadUrl(String fileUrl) {
        throw new UnsupportedOperationException(COS_NOT_WIRED);
    }

    /**
     * 证件附件上传白名单校验（Req 10.6）：非空、大小 ≤ 上限、扩展名与内容类型均在白名单内。
     * 任一不满足抛 {@link InvalidCertificateFileException}。
     */
    private void validateFile(String fileName, long contentLength, String contentType) {
        if (contentLength <= 0) {
            throw new InvalidCertificateFileException("证件文件内容为空");
        }
        long maxBytes = properties.maxFileSize().toBytes();
        if (contentLength > maxBytes) {
            throw new InvalidCertificateFileException(
                    "证件文件大小超过上限 " + properties.maxFileSize().toMegabytes() + "MB");
        }

        String extension = fileExtension(fileName);
        boolean extensionAllowed = extension != null && properties.allowedExtensions().stream()
                .map(this::normalizeExtension)
                .anyMatch(extension::equals);
        if (!extensionAllowed) {
            throw new InvalidCertificateFileException("不支持的证件文件格式：" + fileName);
        }

        boolean contentTypeAllowed = contentType != null && properties.allowedContentTypes().stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(contentType.trim()));
        if (!contentTypeAllowed) {
            throw new InvalidCertificateFileException("不支持的证件文件类型：" + contentType);
        }
    }

    /**
     * 提取文件扩展名（小写，不含点）；无扩展名返回 {@code null}。
     */
    private String fileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 归一化白名单扩展名：去除前导点、转小写。
     */
    private String normalizeExtension(String extension) {
        String normalized = extension.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }
}
