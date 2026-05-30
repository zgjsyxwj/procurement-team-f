import type { SupplierCategory } from '../vo/supplier-info.vo';

/**
 * 创建供应商命令（采购端，Req 6.1-6.3）。
 * <p>
 * sendInvitation=true 为「保存并发送邀请」（→待进入）；false 为「仅保存」（→创建成功）。
 */
export interface CreateSupplierCommand {
  name: string;
  category: SupplierCategory;
  contactName: string;
  contactPhone: string;
  contactEmail: string;
  sendInvitation: boolean;
}
