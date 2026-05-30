-- ============================================================
-- V2: 初始超级管理员数据 (PostgreSQL 16)
-- ============================================================
-- 默认密码: Pass@1234
-- bcrypt hash (cost=10): $2a$10$1N7qPnkC/kieDTcBCl8KhO40xKWEWucTm8gP6CU0bcF5oKhqTTwju

INSERT INTO auth_internal_user (
    name,
    phone,
    email,
    password_hash,
    role,
    status,
    is_super_admin,
    is_first_login,
    failed_attempts,
    created_at,
    updated_at,
    created_by,
    updated_by,
    version
) VALUES (
    '系统管理员',
    '13800000000',
    'admin@ecosaas.com',
    '$2a$10$1N7qPnkC/kieDTcBCl8KhO40xKWEWucTm8gP6CU0bcF5oKhqTTwju',
    'ADMIN',
    'ACTIVE',
    TRUE,
    TRUE,
    0,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM',
    0
);
