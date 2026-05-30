package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository;

import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.AuditLogEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审计日志仓储实现。
 * <p>
 * 支持按事件类型、时间范围、目标账号的动态条件分页查询。
 */
@Repository
@RequiredArgsConstructor
public class JpaAuditLogRepository {

    private final AuditLogJpaDao jpaDao;

    /**
     * 保存审计日志。
     *
     * @param entity 审计日志实体
     * @return 保存后的实体
     */
    public AuditLogEntity save(AuditLogEntity entity) {
        return jpaDao.save(entity);
    }

    /**
     * 按条件分页查询审计日志。
     *
     * @param eventType    事件类型（可选）
     * @param startTime    开始时间（可选）
     * @param endTime      结束时间（可选）
     * @param targetUserId 目标用户ID（可选）
     * @param pageable     分页参数
     * @return 分页审计日志结果
     */
    public Page<AuditLogEntity> findByConditions(String eventType,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime,
                                                  Long targetUserId,
                                                  Pageable pageable) {
        // 无筛选条件时直接分页查询，避免 Specification 生成 WHERE 1=1
        if ((eventType == null || eventType.isBlank())
                && startTime == null && endTime == null && targetUserId == null) {
            return jpaDao.findAll(pageable);
        }
        Specification<AuditLogEntity> spec = buildSpecification(eventType, startTime, endTime, targetUserId);
        return jpaDao.findAll(spec, pageable);
    }

    private Specification<AuditLogEntity> buildSpecification(String eventType,
                                                              LocalDateTime startTime,
                                                              LocalDateTime endTime,
                                                              Long targetUserId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventType != null && !eventType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("eventType"), eventType));
            }

            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }

            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }

            if (targetUserId != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetUserId"), targetUserId));
            }

            if (predicates.isEmpty()) {
                return null; // 无条件时返回 null，避免生成 WHERE 1=1 触发 Druid WallFilter
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
