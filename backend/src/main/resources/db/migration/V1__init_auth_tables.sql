-- ============================================================
-- V1: 认证与权限管理模块 - 数据库表初始化 (PostgreSQL 16)
-- ============================================================

-- 内部用户表
CREATE TABLE auth_internal_user (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,                          -- 姓名
    phone           VARCHAR(20),                                   -- 手机号（SSO用户可能为空）
    email           VARCHAR(128) NOT NULL,                         -- 邮箱
    password_hash   VARCHAR(255),                                  -- 密码哈希（SSO用户可能为空）
    role            VARCHAR(32) NOT NULL,                          -- 角色: ADMIN/BUYER/BUSINESS_USER
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',         -- 状态: ACTIVE/DISABLED
    sso_subject_id  VARCHAR(255),                                  -- SSO用户唯一标识（SAML NameID）
    is_super_admin  BOOLEAN NOT NULL DEFAULT FALSE,                -- 是否超级管理员
    is_first_login  BOOLEAN NOT NULL DEFAULT TRUE,                 -- 是否首次登录
    failed_attempts INT NOT NULL DEFAULT 0,                        -- 连续登录失败次数
    locked_until    TIMESTAMP(3),                                  -- 锁定截止时间
    created_at      TIMESTAMP(3) NOT NULL,
    updated_at      TIMESTAMP(3) NOT NULL,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    version         INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_internal_user_phone ON auth_internal_user (phone) WHERE phone IS NOT NULL;
CREATE UNIQUE INDEX uk_internal_user_email ON auth_internal_user (email);
CREATE UNIQUE INDEX uk_internal_user_sso_subject ON auth_internal_user (sso_subject_id) WHERE sso_subject_id IS NOT NULL;
CREATE INDEX idx_internal_user_role ON auth_internal_user (role);
CREATE INDEX idx_internal_user_status ON auth_internal_user (status);

COMMENT ON TABLE auth_internal_user IS '内部用户表';

-- 供应商用户表
CREATE TABLE auth_supplier_user (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,                          -- 姓名
    phone           VARCHAR(20) NOT NULL,                          -- 手机号
    email           VARCHAR(128) NOT NULL,                         -- 邮箱
    password_hash   VARCHAR(255) NOT NULL,                         -- 密码哈希
    supplier_id     BIGINT NOT NULL,                               -- 关联供应商企业ID
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',         -- 状态: ACTIVE/DISABLED
    is_first_login  BOOLEAN NOT NULL DEFAULT TRUE,                 -- 是否首次登录
    failed_attempts INT NOT NULL DEFAULT 0,                        -- 连续登录失败次数
    locked_until    TIMESTAMP(3),                                  -- 锁定截止时间
    created_at      TIMESTAMP(3) NOT NULL,
    updated_at      TIMESTAMP(3) NOT NULL,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    version         INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_supplier_user_phone ON auth_supplier_user (phone);
CREATE UNIQUE INDEX uk_supplier_user_email ON auth_supplier_user (email);
CREATE INDEX idx_supplier_user_supplier_id ON auth_supplier_user (supplier_id);
CREATE INDEX idx_supplier_user_status ON auth_supplier_user (status);

COMMENT ON TABLE auth_supplier_user IS '供应商用户表';

-- 密码历史表
CREATE TABLE auth_password_history (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,                               -- 用户ID
    user_type       VARCHAR(16) NOT NULL,                          -- 用户类型: INTERNAL/SUPPLIER
    password_hash   VARCHAR(255) NOT NULL,                         -- 历史密码哈希
    created_at      TIMESTAMP(3) NOT NULL
);

CREATE INDEX idx_password_history_user ON auth_password_history (user_id, user_type);

COMMENT ON TABLE auth_password_history IS '密码历史表';

-- 密码重置令牌表
CREATE TABLE auth_password_reset_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,                               -- 用户ID
    user_type       VARCHAR(16) NOT NULL,                          -- 用户类型: INTERNAL/SUPPLIER
    token           VARCHAR(255) NOT NULL,                         -- 重置令牌
    expires_at      TIMESTAMP(3) NOT NULL,                         -- 过期时间
    used            BOOLEAN NOT NULL DEFAULT FALSE,                -- 是否已使用
    created_at      TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX uk_password_reset_token ON auth_password_reset_token (token);
CREATE INDEX idx_password_reset_user ON auth_password_reset_token (user_id, user_type);

COMMENT ON TABLE auth_password_reset_token IS '密码重置令牌表';

-- 采购员-供应商管理关系表
CREATE TABLE auth_buyer_supplier_relation (
    id              BIGSERIAL PRIMARY KEY,
    buyer_id        BIGINT NOT NULL,                               -- 采购员用户ID
    supplier_id     BIGINT NOT NULL,                               -- 供应商企业ID
    source          VARCHAR(32) NOT NULL,                          -- 关系来源: CREATED/PR_ASSIGNED/MANUAL
    created_at      TIMESTAMP(3) NOT NULL,
    created_by      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_buyer_supplier ON auth_buyer_supplier_relation (buyer_id, supplier_id);
CREATE INDEX idx_buyer_supplier_supplier_id ON auth_buyer_supplier_relation (supplier_id);

COMMENT ON TABLE auth_buyer_supplier_relation IS '采购员-供应商管理关系表';

-- 安全审计日志表
CREATE TABLE auth_audit_log (
    id              BIGSERIAL PRIMARY KEY,
    event_type      VARCHAR(32) NOT NULL,                          -- 事件类型
    operator_id     BIGINT,                                        -- 操作人ID（系统操作为NULL）
    operator_name   VARCHAR(64),                                   -- 操作人姓名
    target_user_id  BIGINT,                                        -- 目标账号ID
    target_user_name VARCHAR(64),                                  -- 目标账号姓名
    ip_address      VARCHAR(45) NOT NULL,                          -- IP地址
    result          VARCHAR(16) NOT NULL,                          -- 结果: SUCCESS/FAILURE
    detail          VARCHAR(512),                                  -- 详细信息
    created_at      TIMESTAMP(3) NOT NULL
);

CREATE INDEX idx_audit_log_event_type ON auth_audit_log (event_type);
CREATE INDEX idx_audit_log_created_at ON auth_audit_log (created_at);
CREATE INDEX idx_audit_log_target_user ON auth_audit_log (target_user_id);

COMMENT ON TABLE auth_audit_log IS '安全审计日志表';
