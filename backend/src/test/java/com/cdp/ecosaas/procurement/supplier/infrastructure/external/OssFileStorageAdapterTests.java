package com.cdp.ecosaas.procurement.supplier.infrastructure.external;

import com.cdp.ecosaas.procurement.supplier.infrastructure.config.OssProperties;
import com.cdp.ecosaas.procurement.supplier.shared.exception.InvalidCertificateFileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * OssFileStorageAdapter 单元测试 —— 证件上传白名单校验（格式/大小，Req 10.6）。
 * <p>
 * COS 客户端尚未接入：合法文件通过校验后进入 COS 上传，当前以 UnsupportedOperationException 标记未接入。
 */
class OssFileStorageAdapterTests {

    private final OssFileStorageAdapter adapter = new OssFileStorageAdapter(properties());

    private static OssProperties properties() {
        return new OssProperties(
                "https://cos.ap-shanghai.myqcloud.com",
                "ap-shanghai",
                "test-bucket-1250000000",
                "test-ak",
                "test-sk",
                7200,
                DataSize.ofMegabytes(100),
                List.of("pdf", "jpg", "jpeg", "png"),
                List.of("application/pdf", "image/jpeg", "image/png"));
    }

    private InputStream bytes() {
        return new ByteArrayInputStream(new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("超过大小上限的文件应被拒绝")
    void shouldRejectOversizedFile() {
        long oversized = DataSize.ofMegabytes(100).toBytes() + 1;
        assertThrows(InvalidCertificateFileException.class,
                () -> adapter.upload(bytes(), oversized, "cert.pdf", "application/pdf"));
    }

    @Test
    @DisplayName("不在白名单的扩展名应被拒绝")
    void shouldRejectDisallowedExtension() {
        assertThrows(InvalidCertificateFileException.class,
                () -> adapter.upload(bytes(), 1024, "malware.exe", "application/pdf"));
    }

    @Test
    @DisplayName("缺少扩展名的文件应被拒绝")
    void shouldRejectFileWithoutExtension() {
        assertThrows(InvalidCertificateFileException.class,
                () -> adapter.upload(bytes(), 1024, "certificate", "application/pdf"));
    }

    @Test
    @DisplayName("不在白名单的内容类型应被拒绝")
    void shouldRejectDisallowedContentType() {
        assertThrows(InvalidCertificateFileException.class,
                () -> adapter.upload(bytes(), 1024, "cert.pdf", "application/x-msdownload"));
    }

    @Test
    @DisplayName("空内容（大小<=0）应被拒绝")
    void shouldRejectEmptyContent() {
        assertThrows(InvalidCertificateFileException.class,
                () -> adapter.upload(bytes(), 0, "cert.pdf", "application/pdf"));
    }

    @Test
    @DisplayName("合法文件通过校验后进入 COS 上传（当前未接入故抛 UnsupportedOperationException）")
    void shouldPassValidationThenReachCosStub() {
        // 扩展名大写以验证大小写不敏感
        assertThrows(UnsupportedOperationException.class,
                () -> adapter.upload(bytes(), 2048, "cert.PDF", "application/pdf"));
    }

    @Test
    @DisplayName("生成下载地址当前未接入 COS，抛 UnsupportedOperationException")
    void shouldThrowWhenGenerateDownloadUrlNotWired() {
        assertThrows(UnsupportedOperationException.class,
                () -> adapter.generateDownloadUrl("some/object/key.pdf"));
    }
}
