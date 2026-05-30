import type { CertificateDto } from '../../types/dto/certificate.dto';
import type { CertExpiryStatus } from '../../types/vo/supplier-info.vo';

/**
 * 证件实体（前端领域模型）。
 */
export type Certificate = CertificateDto;

/** 即将到期阈值：截止日在未来 30 天内 */
const EXPIRING_SOON_DAYS = 30;

/** 计算距离截止日的剩余天数（负数表示已过期） */
export function daysUntilExpiry(validTo: string, today: Date = new Date()): number {
  const end = new Date(validTo);
  const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const endDay = new Date(end.getFullYear(), end.getMonth(), end.getDate());
  return Math.round((endDay.getTime() - startOfToday.getTime()) / (24 * 60 * 60 * 1000));
}

/**
 * 派生证件到期状态（客户端兜底；后端查询通常已标注 expiryStatus）。
 */
export function deriveExpiryStatus(validTo: string, today: Date = new Date()): CertExpiryStatus {
  const days = daysUntilExpiry(validTo, today);
  if (days < 0) {
    return 'EXPIRED';
  }
  if (days <= EXPIRING_SOON_DAYS) {
    return 'EXPIRING_SOON';
  }
  return 'NORMAL';
}
