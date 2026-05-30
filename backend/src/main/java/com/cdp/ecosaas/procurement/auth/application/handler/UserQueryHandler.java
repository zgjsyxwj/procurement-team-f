package com.cdp.ecosaas.procurement.auth.application.handler;

import com.cdp.ecosaas.procurement.auth.application.query.UserListQuery;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.InternalUserEntity;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.repository.InternalUserJpaDao;
import com.cdp.ecosaas.procurement.auth.interfaces.dto.UserListResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户列表查询处理器。
 * <p>
 * 支持按角色、状态筛选和按姓名/手机号关键字搜索的分页查询。
 * 返回 DTO 而非 JPA 实体，避免基础设施层泄漏到接口层。
 */
@Service
@RequiredArgsConstructor
public class UserQueryHandler {

    private final InternalUserJpaDao internalUserJpaDao;

    /**
     * 分页查询内部用户列表。
     *
     * @param query 查询参数
     * @return 分页用户响应 DTO
     */
    public UserListResponse query(UserListQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<InternalUserEntity> page;
        // 无筛选条件时直接分页查询，避免 Specification 生成 WHERE 1=1 触发 Druid WallFilter
        if ((query.role() == null || query.role().isBlank())
                && (query.status() == null || query.status().isBlank())
                && (query.keyword() == null || query.keyword().isBlank())) {
            page = internalUserJpaDao.findAll(pageRequest);
        } else {
            Specification<InternalUserEntity> spec = buildSpecification(query);
            page = internalUserJpaDao.findAll(spec, pageRequest);
        }

        return toResponse(page);
    }

    private UserListResponse toResponse(Page<InternalUserEntity> page) {
        var items = page.getContent().stream()
                .map(entity -> new UserListResponse.UserItem(
                        entity.getId(),
                        entity.getName(),
                        entity.getPhone(),
                        entity.getEmail(),
                        entity.getRole(),
                        entity.getStatus(),
                        entity.isSuperAdmin(),
                        entity.isFirstLogin(),
                        entity.getCreatedAt()
                ))
                .toList();

        return new UserListResponse(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Specification<InternalUserEntity> buildSpecification(UserListQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.role() != null && !query.role().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("role"), query.role()));
            }

            if (query.status() != null && !query.status().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.status()));
            }

            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + query.keyword() + "%";
                Predicate nameLike = criteriaBuilder.like(root.get("name"), pattern);
                Predicate phoneLike = criteriaBuilder.like(root.get("phone"), pattern);
                predicates.add(criteriaBuilder.or(nameLike, phoneLike));
            }

            if (predicates.isEmpty()) {
                return null;
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
