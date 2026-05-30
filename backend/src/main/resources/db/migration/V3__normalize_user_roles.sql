-- ============================================================
-- V3: 角色归一化 (PostgreSQL 16)
-- ============================================================
-- 系统仅保留 4 种角色: ADMIN(采购经理) / BUYER(采购员) /
-- BUSINESS_USER(业务人员) / SUPPLIER(供应商)。
-- 历史数据中的 SYSTEM_ADMIN、PROCUREMENT_MANAGER 统一归并为 ADMIN。

UPDATE auth_internal_user
SET role = 'ADMIN',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE role IN ('SYSTEM_ADMIN', 'PROCUREMENT_MANAGER');
