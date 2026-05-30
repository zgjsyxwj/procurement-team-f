/**
 * 证件文件上传适配器 —— 前端预校验（格式/大小，Req 10.6）。
 * <p>
 * 仅允许 PDF/JPG/PNG，单文件 ≤100MB。后端为权威校验，此处为体验优化的前置拦截。
 * 实际上传由 certificate.service 以 multipart 提交至后端，再由后端 OssFileStorageAdapter 存 OSS。
 */

/** 允许的文件扩展名 */
const ALLOWED_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png'];

/** 允许的 MIME 类型 */
const ALLOWED_MIME_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];

/** 单文件大小上限：100MB */
export const MAX_FILE_SIZE = 100 * 1024 * 1024;

export interface FileValidationResult {
  valid: boolean;
  error?: string;
}

/**
 * 校验证件文件格式与大小。
 */
export function validateCertificateFile(file: File): FileValidationResult {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? '';

  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    return { valid: false, error: '仅支持 PDF、JPG、PNG 格式的文件' };
  }

  // 双重校验：MIME 类型（浏览器可识别时）
  if (file.type && !ALLOWED_MIME_TYPES.includes(file.type)) {
    return { valid: false, error: '仅支持 PDF、JPG、PNG 格式的文件' };
  }

  if (file.size > MAX_FILE_SIZE) {
    return { valid: false, error: '文件大小不能超过 100MB' };
  }

  return { valid: true };
}
