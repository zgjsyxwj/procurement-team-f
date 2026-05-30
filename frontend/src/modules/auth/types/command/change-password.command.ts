/**
 * 修改密码命令
 */
export interface ChangePasswordCommand {
  oldPassword: string;
  newPassword: string;
}

/**
 * 忘记密码命令
 */
export interface ForgotPasswordCommand {
  email: string;
}

/**
 * 重置密码命令
 */
export interface ResetPasswordCommand {
  token: string;
  newPassword: string;
}
