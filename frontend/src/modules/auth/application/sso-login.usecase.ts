/**
 * SSO 登录用例
 * 封装 SSO 登录跳转逻辑
 */

/** SSO 配置 */
export interface SsoConfig {
  /** SSO 登录入口 URL */
  ssoLoginUrl: string;
  /** SSO 回调路径 */
  callbackPath: string;
}

/** 默认 SSO 配置 */
const DEFAULT_SSO_CONFIG: SsoConfig = {
  ssoLoginUrl: '/api/internal/auth/sso/login',
  callbackPath: '/api/internal/auth/sso/callback',
};

/**
 * 发起 SSO 登录
 * 重定向浏览器到 Worklife IdP 登录页面
 * @param config 可选的 SSO 配置
 */
export function initiateSsoLogin(config: SsoConfig = DEFAULT_SSO_CONFIG): void {
  window.location.href = config.ssoLoginUrl;
}

/**
 * 获取 SSO 回调路径
 * 用于路由配置中注册 SSO 回调处理
 */
export function getSsoCallbackPath(config: SsoConfig = DEFAULT_SSO_CONFIG): string {
  return config.callbackPath;
}
