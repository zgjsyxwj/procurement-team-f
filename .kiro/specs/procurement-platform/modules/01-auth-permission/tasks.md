# 实施计划：用户认证与权限管理模块

## 概述

基于 DDD 分层架构，按照数据库 → 领域层 → 基础设施层 → 应用层 → 接口层 → 前端的顺序逐步实现认证与权限管理模块。后端使用 Java Spring Boot，前端使用 Vue 3 + TypeScript + ant-design-vue。

## 任务列表

- [x] 1. 数据库 Schema 与基础设施搭建
  - [x] 1.1 创建数据库迁移脚本
    - 创建 `auth_internal_user` 表（含唯一索引 uk_phone, uk_email, uk_sso_subject）
    - 创建 `auth_supplier_user` 表（含唯一索引 uk_phone, uk_email）
    - 创建 `auth_password_history` 表
    - 创建 `auth_password_reset_token` 表（含唯一索引 uk_token）
    - 创建 `auth_buyer_supplier_relation` 表（含唯一索引 uk_buyer_supplier）
    - 创建 `auth_audit_log` 表（含索引 idx_event_type, idx_created_at, idx_target_user）
    - 创建初始超级管理员数据插入脚本
    - _需求: 1.3, 3.1, 6.1, 6.4_

  - [x] 1.2 添加项目依赖配置
    - 添加 JJWT 0.12.6 三模块依赖（jjwt-api, jjwt-impl, jjwt-jackson）
    - 添加 Spring Security SAML 2.0 依赖
    - 添加 bcrypt 相关依赖（Spring Security Crypto）
    - 配置 application.yml 中 JWT 密钥、Token 有效期、SAML IdP 元数据等参数
    - _需求: 1.3, 1.10_

- [x] 2. 领域层 - 模型与枚举
  - [x] 2.1 实现角色与状态枚举
    - 创建 `UserRole` 枚举：ADMIN, BUYER, BUSINESS_USER, SUPPLIER
    - 创建 `UserStatus` 枚举：ACTIVE, DISABLED
    - 创建 `AuditEventType` 枚举：LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, SESSION_TIMEOUT, PASSWORD_CHANGE, PASSWORD_RESET, ACCOUNT_LOCKED, ACCOUNT_UNLOCKED, ACCOUNT_CREATED, ACCOUNT_DISABLED, ACCOUNT_ENABLED, ROLE_CHANGED
    - _需求: 2.1, 6.1_

  - [x] 2.2 实现 InternalUser 聚合根
    - 实现字段：id, name, phone, email, passwordHash, role, status, ssoSubjectId, isSuperAdmin, isFirstLogin, failedAttempts, lockedUntil
    - 实现 `authenticate(rawPassword)` 方法：验证密码，处理失败计数
    - 实现 `lock()` / `unlock()` / `isLocked()` 方法：锁定逻辑含时间判断
    - 实现 `changePassword(oldPwd, newPwd)` 方法
    - 实现 `changeRole(newRole)` 方法：超级管理员不可降级
    - 实现 `disable()` / `enable()` 方法：超级管理员不可停用
    - _需求: 1.1, 1.5, 1.7, 1.8, 1.17, 2.2, 3.1, 3.2_

  - [x] 2.3 实现 SupplierUser 聚合根
    - 实现字段：id, name, phone, email, passwordHash, supplierId, status, isFirstLogin, failedAttempts, lockedUntil
    - 实现 `authenticate(rawPassword)` 方法
    - 实现 `lock()` / `unlock()` / `isLocked()` 方法
    - 实现 `changePassword(oldPwd, newPwd)` 方法
    - _需求: 1.1, 1.7, 1.8, 1.12, 1.14_

  - [x] 2.4 实现值对象与领域事件
    - 创建 `PasswordHistory` 值对象
    - 创建 `LoginAttempt` 值对象
    - 创建领域事件：`UserCreatedEvent`, `UserLockedEvent`, `PasswordChangedEvent`
    - _需求: 4.2, 6.1_


- [x] 3. 领域层 - 领域服务
  - [x] 3.1 实现 PasswordDomainService
    - 实现密码复杂度校验：至少8位，含大写、小写、数字、特殊字符
    - 实现密码历史检查：禁止与最近5次密码相同
    - 实现随机密码生成：满足复杂度要求
    - 实现 bcrypt 哈希与验证
    - _需求: 4.1, 4.2, 4.3, 4.4_

  - [ ]* 3.2 编写 PasswordDomainService 单元测试
    - 测试密码复杂度校验各种边界情况
    - 测试密码历史检查逻辑
    - 测试随机密码生成是否满足策略
    - _需求: 4.1, 4.2, 4.3, 4.4_

  - [x] 3.3 实现 LockoutDomainService
    - 实现登录失败计数递增逻辑
    - 实现锁定判断：连续5次失败触发锁定30分钟
    - 实现自动解锁判断：锁定时间到期后自动解锁并重置计数器
    - 实现手动解锁逻辑
    - _需求: 1.7, 1.8_

  - [ ]* 3.4 编写 LockoutDomainService 单元测试
    - 测试5次失败触发锁定
    - 测试30分钟后自动解锁
    - 测试手动解锁重置计数器
    - _需求: 1.7, 1.8_

  - [x] 3.5 定义领域仓储接口
    - 创建 `InternalUserRepository` 接口：findByPhone, findByEmail, findBySsoSubjectId, save, findById
    - 创建 `SupplierUserRepository` 接口：findByPhone, findByEmail, save, findById
    - _需求: 1.1, 1.12, 1.13_

  - [x] 3.6 定义领域端口接口
    - 创建 `TokenPort` 接口：generateToken, validateToken, invalidateToken
    - 创建 `EmailPort` 接口：sendInitialPassword, sendPasswordResetLink, sendPasswordChangedNotification
    - 创建 `SamlPort` 接口：parseSamlResponse, extractUserAttributes
    - _需求: 1.4, 1.6, 1.10, 5.2_

- [ ] 4. 检查点 - 领域层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [x] 5. 基础设施层 - 持久化
  - [x] 5.1 实现 JPA 实体映射
    - 创建 `InternalUserEntity`：映射 auth_internal_user 表，含乐观锁 @Version
    - 创建 `SupplierUserEntity`：映射 auth_supplier_user 表，含乐观锁 @Version
    - 创建 `PasswordHistoryEntity`：映射 auth_password_history 表
    - 创建 `AuditLogEntity`：映射 auth_audit_log 表
    - 创建 `PasswordResetTokenEntity`：映射 auth_password_reset_token 表
    - 创建 `BuyerSupplierRelationEntity`：映射 auth_buyer_supplier_relation 表
    - _需求: 1.12, 1.13, 4.2, 6.1_

  - [x] 5.2 实现领域对象与实体的 Mapper
    - 创建 `InternalUserMapper`：InternalUser ↔ InternalUserEntity 双向转换
    - 创建 `SupplierUserMapper`：SupplierUser ↔ SupplierUserEntity 双向转换
    - _需求: 1.12, 1.13_

  - [x] 5.3 实现 JPA Repository
    - 创建 `JpaInternalUserRepository`：实现 InternalUserRepository 接口
    - 创建 `JpaSupplierUserRepository`：实现 SupplierUserRepository 接口
    - 创建 `JpaPasswordHistoryRepository`：查询最近5条历史记录
    - 创建 `JpaPasswordResetTokenRepository`：按 token 查询、标记已使用
    - 创建 `JpaAuditLogRepository`：支持按事件类型、时间范围、目标账号分页查询
    - 创建 `JpaBuyerSupplierRelationRepository`：按 buyer_id 和 supplier_id 查询
    - _需求: 1.1, 1.12, 4.2, 5.3, 5.4, 6.3, 2.7_

- [x] 6. 基础设施层 - 安全组件
  - [x] 6.1 实现 JwtTokenProvider
    - 使用 JJWT 0.12.6 新 API（Jwts.builder / Jwts.parser().verifyWith()）
    - 实现 Token 生成：包含 sub(userId), type(INTERNAL/SUPPLIER), role, name, iat, exp
    - 实现 Token 验证与解析
    - Token 有效期30分钟，支持滑动过期（每次请求刷新）
    - _需求: 1.9, 1.15_

  - [ ]* 6.2 编写 JwtTokenProvider 单元测试
    - 测试 Token 生成与解析
    - 测试过期 Token 验证失败
    - 测试篡改 Token 验证失败
    - _需求: 1.9_

  - [x] 6.3 实现 JwtAuthenticationFilter
    - 从 Cookie 中提取 JWT Token（非 Authorization Header）
    - 验证 Token 有效性
    - 设置 SecurityContext
    - 实现滑动过期：验证通过后刷新 Cookie 过期时间
    - CSRF Token 校验：写操作验证请求头中的 X-CSRF-TOKEN 与 Cookie 一致
    - _需求: 1.9, 1.15_

  - [x] 6.4 实现 SecurityConfig
    - 配置 Spring Security 过滤链
    - 配置公开路径：登录、SSO回调、忘记密码、重置密码接口
    - 配置角色权限：/api/admin/** 仅 ADMIN 角色可访问
    - 配置 CORS 策略
    - 配置 CSRF Double Submit Cookie 模式
    - 禁用 Spring Security 默认 session 管理（使用 JWT 无状态）
    - _需求: 2.6, 2.8_

  - [x] 6.5 实现 SamlAuthProvider
    - 配置 SAML 2.0 Service Provider 元数据
    - 实现 SAML Response 解析：提取 NameID、姓名、邮箱、手机号（可选）
    - 实现 JIT Provisioning 逻辑：首次 SSO 登录自动创建本地账号
    - 默认角色为 BUSINESS_USER
    - _需求: 1.10, 1.11_

  - [x] 6.6 实现 EmailServiceAdapter
    - 实现 EmailPort 接口
    - 实现发送初始密码邮件
    - 实现发送密码重置链接邮件（含30分钟有效期说明）
    - 实现发送密码变更通知邮件
    - _需求: 1.4, 1.6, 5.2, 5.6_


- [ ] 7. 检查点 - 基础设施层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [x] 8. 应用层 - 命令与查询
  - [x] 8.1 实现认证相关 Command 与 Handler
    - 创建 `LoginCommand`：phone, password, userType(INTERNAL/SUPPLIER)
    - 创建 `SsoLoginCommand`：samlResponse
    - 创建 `LogoutCommand`：userId, userType
    - 实现 `AuthCommandHandler`：
      - 处理登录：验证凭据 → 检查锁定 → 检查状态 → 生成Token → 记录审计日志
      - 处理SSO登录：解析SAML → JIT创建/查找用户 → 生成Token → 记录审计日志
      - 处理登出：销毁会话 → 记录审计日志
    - _需求: 1.1, 1.2, 1.7, 1.10, 1.11, 1.16, 1.17, 6.1_

  - [x] 8.2 实现密码相关 Command 与 Handler
    - 创建 `ChangePasswordCommand`：userId, userType, oldPassword, newPassword
    - 创建 `ForgotPasswordCommand`：email, userType
    - 创建 `ResetPasswordCommand`：token, newPassword
    - 创建 `AdminResetPasswordCommand`：targetUserId
    - 实现密码处理逻辑：
      - 修改密码：验证旧密码 → 校验策略 → 检查历史 → 更新 → 记录历史 → 审计日志
      - 忘记密码：查找用户 → 生成重置Token → 发送邮件（不透露邮箱是否存在）
      - 重置密码：验证Token有效性和时效 → 校验策略 → 更新密码 → 标记Token已用 → 审计日志
      - 管理员重置：生成随机密码 → 更新 → 邮件通知 → 审计日志
    - _需求: 1.5, 1.6, 4.1, 4.2, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [ ]* 8.3 编写认证与密码 Handler 单元测试
    - 测试登录成功流程
    - 测试登录失败（错误密码、账号锁定、账号停用）
    - 测试密码修改（旧密码验证、策略校验、历史检查）
    - 测试忘记密码（邮箱存在/不存在统一响应）
    - 测试重置密码（Token过期、Token已用、策略校验）
    - _需求: 1.1, 1.2, 1.5, 1.7, 1.17, 5.3, 5.4, 5.7_

  - [x] 8.4 实现用户管理 Command 与 Handler
    - 创建 `CreateInternalUserCommand`：name, phone, email, role
    - 创建 `UpdateUserRoleCommand`：userId, newRole
    - 创建 `UpdateUserStatusCommand`：userId, newStatus
    - 创建 `UnlockUserCommand`：userId
    - 实现 `UserCommandHandler`：
      - 创建用户：校验手机号/邮箱唯一 → 生成初始密码 → 保存 → 发送邮件 → 审计日志
      - 修改角色：校验超级管理员不可降级 → 更新 → 审计日志
      - 停用/启用：校验超级管理员不可停用 → 更新 → 审计日志
      - 手动解锁：重置失败计数和锁定时间 → 审计日志
    - _需求: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 6.1_

  - [x] 8.5 实现用户列表与审计日志查询
    - 创建 `UserListQuery`：role, status, keyword(姓名/手机号), page, size
    - 创建 `AuditLogQuery`：eventType, startTime, endTime, targetUserId, page, size
    - 实现 `AuditLogQueryHandler`：分页查询审计日志
    - 实现用户列表查询：支持按角色、状态筛选和关键字搜索
    - _需求: 3.8, 6.3_

- [x] 9. 接口层 - REST Controller
  - [x] 9.1 实现认证相关 DTO
    - 创建 `LoginRequest`：phone, password
    - 创建 `LoginResponse`：userInfo(id, name, role, isFirstLogin)
    - 创建 `ChangePasswordRequest`：oldPassword, newPassword
    - 创建 `ForgotPasswordRequest`：email
    - 创建 `ResetPasswordRequest`：token, newPassword
    - 创建 `CreateUserRequest`：name, phone, email, role
    - 创建 `UserListResponse`：分页用户列表
    - 创建 `AuditLogResponse`：审计日志列表
    - _需求: 1.1, 1.5, 3.3, 5.1_

  - [x] 9.2 实现 AuthController（内部用户认证）
    - `POST /api/internal/auth/login`：内部用户手机号密码登录，成功后 Set-Cookie(JWT, HttpOnly) + Set-Cookie(X-CSRF-TOKEN)
    - `POST /api/internal/auth/sso/callback`：处理 SAML Response 回调
    - `POST /api/auth/logout`：清除 Cookie（Max-Age=0）
    - `POST /api/auth/change-password`：修改密码
    - `POST /api/auth/forgot-password`：发送重置邮件
    - `POST /api/auth/reset-password`：通过 Token 重置密码
    - _需求: 1.1, 1.2, 1.5, 1.10, 1.16, 5.1, 5.2_

  - [x] 9.3 实现 SupplierAuthController（供应商认证）
    - `POST /api/supplier/auth/login`：供应商手机号密码登录，成功后 Set-Cookie + CSRF Token
    - 返回 isFirstLogin 标志供前端弹窗提示修改密码
    - _需求: 1.12, 1.14_

  - [x] 9.4 实现 InternalUserController（用户管理）
    - `GET /api/admin/users`：查询内部用户列表（分页、筛选、搜索）
    - `POST /api/admin/users`：创建内部用户
    - `PATCH /api/admin/users/{id}/role`：修改用户角色
    - `PATCH /api/admin/users/{id}/status`：停用/启用用户
    - `POST /api/admin/users/{id}/reset-password`：重置用户密码
    - `POST /api/admin/users/{id}/unlock`：手动解锁用户
    - _需求: 3.1, 3.2, 3.3, 3.5, 3.6, 3.7, 3.8_

  - [x] 9.5 实现 AuditLogController
    - `GET /api/admin/audit-logs`：分页查询审计日志，支持按事件类型、时间范围、目标账号筛选
    - _需求: 6.3_

  - [ ]* 9.6 编写 Controller 集成测试
    - 测试登录接口（成功、失败、锁定）
    - 测试密码修改接口
    - 测试用户管理接口（权限校验）
    - 测试审计日志查询接口
    - _需求: 1.1, 1.2, 1.7, 2.6, 3.3, 6.3_

- [ ] 10. 检查点 - 后端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [x] 11. 前端 - 基础设施与状态管理
  - [x] 11.1 实现前端类型定义
    - 创建 `login.dto.ts`：LoginRequest, LoginResponse 类型
    - 创建 `user.dto.ts`：CreateUserRequest, UserListResponse, AuditLogResponse 类型
    - 创建 `user-info.vo.ts`：UserInfo 值对象（id, name, role, isFirstLogin）
    - 创建 `login.command.ts`、`change-password.command.ts` 命令类型
    - _需求: 1.1, 3.3_

  - [x] 11.2 实现 auth.service.ts（API 调用层）
    - 实现 `login(phone, password)`：POST /api/internal/auth/login
    - 实现 `supplierLogin(phone, password)`：POST /api/supplier/auth/login
    - 实现 `logout()`：POST /api/auth/logout
    - 实现 `changePassword(oldPwd, newPwd)`：POST /api/auth/change-password
    - 实现 `forgotPassword(email)`：POST /api/auth/forgot-password
    - 实现 `resetPassword(token, newPwd)`：POST /api/auth/reset-password
    - 配置 CSRF Token：从 Cookie 读取 X-CSRF-TOKEN 并在写操作请求头中携带
    - _需求: 1.1, 1.5, 1.16, 5.1_

  - [x] 11.3 实现 user-management.service.ts（管理 API）
    - 实现 `getUserList(query)`：GET /api/admin/users
    - 实现 `createUser(data)`：POST /api/admin/users
    - 实现 `updateUserRole(id, role)`：PATCH /api/admin/users/{id}/role
    - 实现 `updateUserStatus(id, status)`：PATCH /api/admin/users/{id}/status
    - 实现 `resetUserPassword(id)`：POST /api/admin/users/{id}/reset-password
    - 实现 `unlockUser(id)`：POST /api/admin/users/{id}/unlock
    - 实现 `getAuditLogs(query)`：GET /api/admin/audit-logs
    - _需求: 3.3, 3.5, 3.6, 3.7, 3.8, 6.3_

  - [x] 11.4 实现 csrf-token.adapter.ts
    - 实现从 Cookie 中读取 CSRF Token
    - 配置 axios 拦截器：在 POST/PATCH/DELETE 请求头中自动添加 X-CSRF-TOKEN
    - _需求: 设计文档 Token 存储策略_

  - [x] 11.5 实现 auth.store.ts（Pinia 状态管理）
    - 存储当前用户信息：id, name, role, isFirstLogin
    - 实现 login action：调用 service → 存储用户信息 → 路由跳转
    - 实现 logout action：调用 service → 清除状态 → 跳转登录页
    - 实现 checkAuth action：页面刷新时从后端获取当前用户信息
    - 实现 getter：isAdmin, isBuyer, isBusinessUser, isSupplier
    - _需求: 1.1, 1.16, 2.1_

- [x] 12. 前端 - 领域与用例层
  - [x] 12.1 实现前端领域模型
    - 创建 `user.entity.ts`：User 实体定义
    - 创建 `password-policy.vo.ts`：密码策略值对象
    - 创建 `password-validation.rule.ts`：前端密码复杂度校验规则
    - _需求: 4.1, 4.3_

  - [x] 12.2 实现前端用例层
    - 创建 `login.usecase.ts`：封装登录业务逻辑（表单验证 → 调用service → 处理响应）
    - 创建 `sso-login.usecase.ts`：封装 SSO 登录跳转逻辑
    - 创建 `change-password.usecase.ts`：封装修改密码逻辑（前端策略校验 → 调用service）
    - 创建 `forgot-password.usecase.ts`：封装忘记密码流程
    - 创建 `manage-users.usecase.ts`：封装用户管理操作
    - _需求: 1.1, 1.5, 1.10, 5.1, 3.3_


- [x] 13. 前端 - 页面与组件
  - [x] 13.1 实现内部用户登录页 InternalLoginView.vue
    - 手机号密码登录表单（含表单验证）
    - SSO 登录按钮（跳转 Worklife IdP）
    - "忘记密码"链接入口
    - 登录失败错误提示（统一提示"手机号或密码错误"）
    - 账号锁定提示（"账号已锁定，请30分钟后重试或联系管理员"）
    - 账号停用提示（"账号已停用，请联系管理员"）
    - _需求: 1.1, 1.2, 1.7, 1.10, 1.17, 5.1_

  - [x] 13.2 实现供应商登录页 SupplierLoginView.vue
    - 手机号密码登录表单（含表单验证）
    - "忘记密码"链接入口
    - 登录失败/锁定/停用错误提示
    - 首次登录后弹窗建议修改密码（可跳过）
    - _需求: 1.12, 1.14, 5.1_

  - [x] 13.3 实现忘记密码页面 ForgotPasswordView.vue
    - 邮箱输入表单
    - 提交后显示统一提示"如果该邮箱已注册，您将收到重置邮件"
    - _需求: 5.1, 5.2, 5.7_

  - [x] 13.4 实现重置密码页面 ResetPasswordView.vue
    - 从 URL 参数获取重置 Token
    - 新密码输入表单（含密码复杂度实时校验提示）
    - 确认密码一致性校验
    - Token 过期/已使用错误处理
    - 重置成功后跳转登录页
    - _需求: 5.3, 5.4, 5.5_

  - [x] 13.5 实现修改密码对话框 ChangePasswordDialog.vue
    - 旧密码、新密码、确认密码输入
    - 密码复杂度实时校验与提示（显示具体不满足项）
    - 修改成功后提示并可选重新登录
    - _需求: 1.5, 4.1, 4.3_

  - [x] 13.6 实现 usePasswordValidation composable
    - 实时校验密码复杂度：长度≥8、含大写、含小写、含数字、含特殊字符
    - 返回各项校验结果供 UI 展示
    - _需求: 4.1, 4.3_

  - [x] 13.7 实现用户管理页面 UserManagementView.vue
    - 用户列表表格（ant-design-vue Table 组件）
    - 支持按角色、状态筛选
    - 支持按姓名/手机号搜索
    - 分页功能
    - 操作列：修改角色、停用/启用、重置密码、解锁
    - 创建用户按钮与表单弹窗
    - _需求: 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

  - [x] 13.8 实现审计日志页面（集成到 UserManagementView 或独立 Tab）
    - 审计日志列表表格
    - 支持按事件类型、时间范围、目标账号筛选
    - 分页功能
    - _需求: 6.3_

- [x] 14. 前端 - 路由与权限守卫
  - [x] 14.1 实现 auth.routes.ts 路由配置
    - 配置内部用户登录路由：/internal/login
    - 配置供应商登录路由：/supplier/login
    - 配置忘记密码路由：/forgot-password
    - 配置重置密码路由：/reset-password
    - 配置用户管理路由：/admin/users（仅 ADMIN 角色）
    - 配置审计日志路由：/admin/audit-logs（仅 ADMIN 角色）
    - _需求: 2.8, 2.12_

  - [x] 14.2 实现路由守卫与权限控制
    - 实现全局前置守卫：未认证用户重定向到登录页
    - 实现角色权限守卫：非 ADMIN 用户访问管理页面时重定向
    - 实现会话超时检测：后端返回 401 时跳转登录页
    - 实现页面入口隔离：业务人员/采购员/供应商各自路由互不可见
    - _需求: 1.9, 2.6, 2.8, 2.11_

  - [x] 14.3 实现 useAuth composable
    - 封装认证状态判断逻辑
    - 提供 currentUser, isAuthenticated, hasRole 等响应式属性
    - 封装登出逻辑
    - _需求: 2.1, 2.6_

- [ ] 15. 检查点 - 前端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [x] 16. 集成与端到端验证
  - [x] 16.1 实现共享常量与异常处理
    - 创建 `AuthConstants.java`：JWT 配置常量、Cookie 名称、锁定阈值等
    - 创建自定义异常：`AuthenticationException`, `AccountLockedException`, `PasswordPolicyViolationException`
    - 实现全局异常处理器：统一错误响应格式
    - _需求: 1.2, 1.7, 2.6, 4.3_

  - [x] 16.2 实现审计日志切面/拦截器
    - 创建审计日志记录服务：统一记录安全事件
    - 在各 Handler 中集成审计日志记录
    - 确保记录：事件类型、操作时间、操作人、目标账号、IP地址、操作结果
    - _需求: 6.1, 6.2_

  - [x] 16.3 前后端联调验证
    - 验证内部用户手机号密码登录完整流程（Cookie 下发与携带）
    - 验证供应商登录完整流程
    - 验证 CSRF Token 机制（写操作需携带 Token）
    - 验证会话超时与滑动过期
    - 验证密码修改与重置流程
    - 验证用户管理操作（创建、角色修改、停用、解锁）
    - _需求: 1.1, 1.5, 1.9, 1.12, 3.3_

  - [ ]* 16.4 编写端到端集成测试
    - 测试完整登录→操作→登出流程
    - 测试账号锁定与自动解锁流程
    - 测试密码重置完整流程
    - 测试权限隔离（非管理员访问管理接口返回403）
    - _需求: 1.1, 1.7, 1.8, 2.6, 5.1_

- [ ] 17. 最终检查点 - 全模块完成
  - 确保所有测试通过，如有疑问请向用户确认。

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加速 MVP 交付
- 每个任务引用了对应的需求编号以确保可追溯性
- 检查点任务确保增量验证
- 后端与前端可并行开发，但联调需在双方基础功能完成后进行
- 密码相关逻辑（复杂度校验、历史检查）前后端均需实现，后端为权威校验
