import type { BankAccountDto } from '../dto/supplier.dto';

/**
 * 供应商端提交信息变更用例输入（Req 3.3、3.9、4.2）。
 * <p>
 * 后端 `PUT /api/supplier/profile` 仅接收基本信息 `changedFields`（按字段级 diff 生成变更明细）；
 * 银行多值变更后端尚未支持（BANK 变更延后），此处 bankAccounts 仅供前端校验/展示，不随该接口持久化。
 */
export interface SubmitChangeInput {
  /** 基本信息字段（key -> 值，空串表示清空） */
  basicInfo: Record<string, string>;
  /** 银行信息（前端编辑态；后端 BANK 变更就绪前不持久化） */
  bankAccounts: BankAccountDto[];
}
