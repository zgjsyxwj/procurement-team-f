-- ============================================================
-- V4: 供应商管理模块 - 数据库表初始化 (PostgreSQL 16)
-- ============================================================

-- 供应商企业表
CREATE TABLE supplier (
    id                          BIGSERIAL PRIMARY KEY,
    supplier_code               VARCHAR(16) NOT NULL,              -- 供应商ID号(VD+4位自增)
    name                        VARCHAR(128) NOT NULL,             -- 供应商名称
    category                    VARCHAR(16) NOT NULL,              -- 分类: DOMESTIC/OVERSEAS
    status                      VARCHAR(20) NOT NULL DEFAULT 'CREATED', -- 状态机
    unified_social_credit_code  VARCHAR(32),                       -- 统一社会信用代码
    legal_person                VARCHAR(64),                       -- 公司法人
    registered_date             DATE,                              -- 注册时间
    registered_capital          NUMERIC(18,2),                     -- 注册资金(正数)
    address                     VARCHAR(255),                      -- 公司地址
    general_taxpayer            BOOLEAN,                           -- 一般纳税人
    business_scope              TEXT,                              -- 经营范围(选填)
    enterprise_nature           VARCHAR(64),                       -- 企业性质(选填)
    sales_mode                  VARCHAR(64),                       -- 销售模式(选填)
    coverage_area               VARCHAR(255),                      -- 覆盖区域(选填)
    annual_revenue              NUMERIC(18,2),                     -- 本年度营业额(选填)
    employee_count              INT,                               -- 员工人数(选填)
    main_customers              VARCHAR(512),                      -- 主力客户(选填)
    created_at                  TIMESTAMP(3) NOT NULL,
    updated_at                  TIMESTAMP(3) NOT NULL,
    created_by                  VARCHAR(64),
    updated_by                  VARCHAR(64),
    version                     INT NOT NULL DEFAULT 0             -- 乐观锁
);

CREATE UNIQUE INDEX uk_supplier_code ON supplier (supplier_code);
CREATE INDEX idx_supplier_status ON supplier (status);
CREATE INDEX idx_supplier_name ON supplier (name);

COMMENT ON TABLE supplier IS '供应商企业表';

-- 供应商银行账号表
CREATE TABLE supplier_bank_account (
    id              BIGSERIAL PRIMARY KEY,
    supplier_id     BIGINT NOT NULL,                               -- 所属供应商
    account_name    VARCHAR(128) NOT NULL,                         -- 户名
    bank_name       VARCHAR(128) NOT NULL,                         -- 开户银行名称
    account_number  VARCHAR(64) NOT NULL,                          -- 银行账号
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(3) NOT NULL,
    updated_at      TIMESTAMP(3) NOT NULL
);

CREATE INDEX idx_bank_account_supplier ON supplier_bank_account (supplier_id);

COMMENT ON TABLE supplier_bank_account IS '供应商银行账号表';

-- 供应商联系人表
CREATE TABLE supplier_contact (
    id              BIGSERIAL PRIMARY KEY,
    supplier_id     BIGINT NOT NULL,                               -- 所属供应商
    name            VARCHAR(64) NOT NULL,                          -- 姓名(必填)
    phone           VARCHAR(20) NOT NULL,                          -- 手机号(必填)
    email           VARCHAR(128) NOT NULL,                         -- 邮箱(必填)
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,                -- 是否主要联系人
    position        VARCHAR(64),                                   -- 职务(选填)
    department      VARCHAR(64),                                   -- 部门(选填)
    created_at      TIMESTAMP(3) NOT NULL,
    updated_at      TIMESTAMP(3) NOT NULL,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64)
);

CREATE INDEX idx_contact_supplier ON supplier_contact (supplier_id);
CREATE UNIQUE INDEX uk_contact_primary ON supplier_contact (supplier_id) WHERE is_primary = TRUE;

COMMENT ON TABLE supplier_contact IS '供应商联系人表';

-- 证件类型字典表
CREATE TABLE supplier_certificate_type (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,                          -- 证件类型名称
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',         -- ACTIVE/DISABLED
    remark          VARCHAR(255),
    created_at      TIMESTAMP(3) NOT NULL,
    updated_at      TIMESTAMP(3) NOT NULL,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64)
);

CREATE UNIQUE INDEX uk_cert_type_name ON supplier_certificate_type (name);

COMMENT ON TABLE supplier_certificate_type IS '证件类型字典表';

-- 证件类型差异化字段表
CREATE TABLE supplier_cert_type_field (
    id              BIGSERIAL PRIMARY KEY,
    cert_type_id    BIGINT NOT NULL,                               -- 所属证件类型
    field_key       VARCHAR(64) NOT NULL,                          -- 字段标识
    field_label     VARCHAR(64) NOT NULL,                          -- 字段显示名
    field_type      VARCHAR(32) NOT NULL,                          -- TEXT/NUMBER/DATE/SELECT...
    required        BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(3) NOT NULL
);

CREATE INDEX idx_cert_type_field_type ON supplier_cert_type_field (cert_type_id);

COMMENT ON TABLE supplier_cert_type_field IS '证件类型差异化字段表';

-- 供应商证件表
CREATE TABLE supplier_certificate (
    id               BIGSERIAL PRIMARY KEY,
    supplier_id      BIGINT NOT NULL,                              -- 所属供应商
    cert_type_id     BIGINT NOT NULL,                             -- 证件类型
    file_url         VARCHAR(512) NOT NULL,                        -- OSS 访问标识
    file_name        VARCHAR(255) NOT NULL,                        -- 原始文件名
    valid_from       DATE NOT NULL,                                -- 有效期起始
    valid_to         DATE NOT NULL,                                -- 有效期截止
    audit_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW', -- 待审核/已通过/驳回
    reject_reason    VARCHAR(255),                                 -- 驳回原因(可选)
    source           VARCHAR(20) NOT NULL,                         -- SUPPLIER_UPLOAD/BUYER_MAINTAIN
    is_current_valid BOOLEAN NOT NULL DEFAULT TRUE,                -- 是否当前有效(否=历史版本)
    extra_fields     JSONB,                                        -- 差异化字段提交值
    maintained_by    VARCHAR(64),                                  -- 维护人
    created_at       TIMESTAMP(3) NOT NULL,
    updated_at       TIMESTAMP(3) NOT NULL
);

CREATE INDEX idx_certificate_supplier ON supplier_certificate (supplier_id);
CREATE INDEX idx_certificate_audit_status ON supplier_certificate (audit_status);
CREATE INDEX idx_certificate_valid_to ON supplier_certificate (valid_to);

COMMENT ON TABLE supplier_certificate IS '供应商证件表';

-- 供应商信息变更申请/记录表
CREATE TABLE supplier_change_request (
    id              BIGSERIAL PRIMARY KEY,
    supplier_id     BIGINT NOT NULL,                               -- 所属供应商
    change_type     VARCHAR(20) NOT NULL,                          -- BASIC_INFO/BANK
    source          VARCHAR(20) NOT NULL,                          -- SUPPLIER/BUYER
    status          VARCHAR(20) NOT NULL,                          -- 待审核/已通过/驳回/已撤回
    submitter_id    BIGINT NOT NULL,                               -- 提交人ID
    submitter_name  VARCHAR(64) NOT NULL,                          -- 提交人姓名
    submitted_at    TIMESTAMP(3) NOT NULL,                         -- 提交时间
    reviewer_id     BIGINT,                                        -- 审核人ID
    reviewer_name   VARCHAR(64),                                   -- 审核人姓名
    reviewed_at     TIMESTAMP(3),                                  -- 审核时间
    review_comment  VARCHAR(255),                                  -- 审核意见/驳回原因
    withdrawn_at    TIMESTAMP(3),                                  -- 撤回时间
    reminded_at     TIMESTAMP(3),                                  -- 24h 提醒时间
    created_at      TIMESTAMP(3) NOT NULL
);

CREATE INDEX idx_change_supplier ON supplier_change_request (supplier_id);
CREATE INDEX idx_change_status ON supplier_change_request (status);
CREATE UNIQUE INDEX uk_change_pending ON supplier_change_request (supplier_id, change_type)
    WHERE status = 'PENDING_REVIEW';

COMMENT ON TABLE supplier_change_request IS '供应商信息变更申请/记录表';

-- 供应商变更字段明细表
CREATE TABLE supplier_change_field (
    id                  BIGSERIAL PRIMARY KEY,
    change_request_id   BIGINT NOT NULL,                           -- 所属变更
    field_key           VARCHAR(64) NOT NULL,                      -- 字段标识
    field_label         VARCHAR(64) NOT NULL,                      -- 字段显示名
    before_value        TEXT,                                      -- 变更前值
    after_value         TEXT                                       -- 变更后值
);

CREATE INDEX idx_change_field_request ON supplier_change_field (change_request_id);

COMMENT ON TABLE supplier_change_field IS '供应商变更字段明细表';

-- 供应商邀请邮件发送日志表
CREATE TABLE supplier_invitation_log (
    id              BIGSERIAL PRIMARY KEY,
    supplier_id     BIGINT NOT NULL,                               -- 所属供应商
    contact_id      BIGINT,                                        -- 收件联系人(可空)
    recipient_email VARCHAR(128) NOT NULL,                         -- 收件邮箱
    sent_by         VARCHAR(64),                                   -- 发送人
    sent_at         TIMESTAMP(3) NOT NULL,                         -- 发送时间
    result          VARCHAR(16) NOT NULL                           -- SUCCESS/FAILURE
);

CREATE INDEX idx_invitation_supplier ON supplier_invitation_log (supplier_id);

COMMENT ON TABLE supplier_invitation_log IS '供应商邀请邮件发送日志表';

-- 证件到期提醒去重日志表
CREATE TABLE supplier_cert_reminder_log (
    id              BIGSERIAL PRIMARY KEY,
    certificate_id  BIGINT NOT NULL,                               -- 证件ID
    remind_node     INT NOT NULL,                                  -- 提醒节点天数(30/15/7/3/0)
    sent_at         TIMESTAMP(3) NOT NULL
);

CREATE UNIQUE INDEX uk_cert_reminder_node ON supplier_cert_reminder_log (certificate_id, remind_node);

COMMENT ON TABLE supplier_cert_reminder_log IS '证件到期提醒去重日志表';
