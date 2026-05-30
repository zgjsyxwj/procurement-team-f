/**
 * 密码策略值对象
 * 定义密码复杂度要求的配置
 */
export interface PasswordPolicy {
  /** 最小长度 */
  minLength: number;
  /** 是否要求包含大写字母 */
  requireUppercase: boolean;
  /** 是否要求包含小写字母 */
  requireLowercase: boolean;
  /** 是否要求包含数字 */
  requireDigit: boolean;
  /** 是否要求包含特殊字符 */
  requireSpecialChar: boolean;
}

/**
 * 默认密码策略
 * 至少8位，含大写、小写、数字、特殊字符
 */
export const DEFAULT_PASSWORD_POLICY: PasswordPolicy = {
  minLength: 8,
  requireUppercase: true,
  requireLowercase: true,
  requireDigit: true,
  requireSpecialChar: true,
};
