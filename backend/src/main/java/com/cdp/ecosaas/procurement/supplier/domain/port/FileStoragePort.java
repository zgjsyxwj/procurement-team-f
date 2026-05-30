package com.cdp.ecosaas.procurement.supplier.domain.port;

import java.io.InputStream;

/**
 * 文件存储端口（出站，对接腾讯云 COS 对象存储）。
 * <p>
 * 证件文件存于 COS，库内仅存访问标识；下载时生成临时访问地址（Req 10.3）。
 * 实现见 {@code OssFileStorageAdapter}（任务 6.1）。
 */
public interface FileStoragePort {

    /**
     * 上传文件，返回对象存储访问标识（objectKey / 持久化于 {@code supplier_certificate.file_url}）。
     */
    String upload(InputStream content, long contentLength, String fileName, String contentType);

    /**
     * 为已存储对象生成临时下载访问地址。
     */
    String generateDownloadUrl(String fileUrl);
}
