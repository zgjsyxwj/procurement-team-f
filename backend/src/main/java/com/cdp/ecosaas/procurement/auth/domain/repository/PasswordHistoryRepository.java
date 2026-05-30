package com.cdp.ecosaas.procurement.auth.domain.repository;

import com.cdp.ecosaas.procurement.auth.domain.model.PasswordHistory;

import java.util.List;

/**
 * 密码历史仓储接口（领域层端口）
 * <p>
 * 定义密码历史持久化的抽象契约，由基础设施层实现。
 */
public interface PasswordHistoryRepository {

    /**
     * 查询指定用户最近的密码历史记录。
     *
     * @param userId   用户ID
     * @param userType 用户类型（INTERNAL / SUPPLIER）
     * @return 最近的密码历史记录列表（按时间倒序，最多5条）
     */
    List<PasswordHistory> findRecentByUser(Long userId, String userType);

    /**
     * 保存密码历史记录。
     *
     * @param passwordHistory 密码历史领域对象
     */
    void save(PasswordHistory passwordHistory);
}
