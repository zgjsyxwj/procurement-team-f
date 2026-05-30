import { ref, computed, watch } from 'vue';
import { validatePassword } from '../../domain/rules/password-validation.rule';
import type { PasswordValidationResult, PasswordValidationDetails } from '../../domain/rules/password-validation.rule';

/**
 * 密码复杂度实时校验 composable
 * 返回各项校验结果供 UI 展示
 */
export function usePasswordValidation() {
  const password = ref('');

  const validationResult = computed<PasswordValidationResult>(() => {
    if (!password.value) {
      return {
        isValid: false,
        errors: [],
        details: {
          lengthOk: false,
          uppercaseOk: false,
          lowercaseOk: false,
          digitOk: false,
          specialCharOk: false,
        },
      };
    }
    return validatePassword(password.value);
  });

  const isValid = computed(() => validationResult.value.isValid);
  const errors = computed(() => validationResult.value.errors);
  const details = computed<PasswordValidationDetails>(() => validationResult.value.details);

  /**
   * 校验规则列表，用于 UI 展示各项是否通过
   */
  const rules = computed(() => [
    { label: '至少8位字符', passed: details.value.lengthOk },
    { label: '包含大写字母', passed: details.value.uppercaseOk },
    { label: '包含小写字母', passed: details.value.lowercaseOk },
    { label: '包含数字', passed: details.value.digitOk },
    { label: '包含特殊字符', passed: details.value.specialCharOk },
  ]);

  return {
    password,
    validationResult,
    isValid,
    errors,
    details,
    rules,
  };
}
