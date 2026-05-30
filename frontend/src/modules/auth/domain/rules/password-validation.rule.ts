import { DEFAULT_PASSWORD_POLICY, type PasswordPolicy } from '../value-objects/password-policy.vo';

/**
 * 密码校验详情
 */
export interface PasswordValidationDetails {
  /** 长度是否满足 */
  lengthOk: boolean;
  /** 是否包含大写字母 */
  uppercaseOk: boolean;
  /** 是否包含小写字母 */
  lowercaseOk: boolean;
  /** 是否包含数字 */
  digitOk: boolean;
  /** 是否包含特殊字符 */
  specialCharOk: boolean;
}

/**
 * 密码校验结果
 */
export interface PasswordValidationResult {
  /** 是否通过所有校验 */
  isValid: boolean;
  /** 不满足项的错误提示列表 */
  errors: string[];
  /** 各项校验详情 */
  details: PasswordValidationDetails;
}

/**
 * 校验密码是否满足复杂度要求
 * @param password 待校验的密码
 * @param policy 密码策略，默认使用系统默认策略
 * @returns 校验结果
 */
export function validatePassword(
  password: string,
  policy: PasswordPolicy = DEFAULT_PASSWORD_POLICY,
): PasswordValidationResult {
  const errors: string[] = [];

  const lengthOk = password.length >= policy.minLength;
  if (!lengthOk) {
    errors.push(`密码长度至少为 ${policy.minLength} 位`);
  }

  const uppercaseOk = !policy.requireUppercase || /[A-Z]/.test(password);
  if (!uppercaseOk) {
    errors.push('密码必须包含至少一个大写字母');
  }

  const lowercaseOk = !policy.requireLowercase || /[a-z]/.test(password);
  if (!lowercaseOk) {
    errors.push('密码必须包含至少一个小写字母');
  }

  const digitOk = !policy.requireDigit || /\d/.test(password);
  if (!digitOk) {
    errors.push('密码必须包含至少一个数字');
  }

  const specialCharOk = !policy.requireSpecialChar || /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/.test(password);
  if (!specialCharOk) {
    errors.push('密码必须包含至少一个特殊字符');
  }

  const details: PasswordValidationDetails = {
    lengthOk,
    uppercaseOk,
    lowercaseOk,
    digitOk,
    specialCharOk,
  };

  return {
    isValid: errors.length === 0,
    errors,
    details,
  };
}
