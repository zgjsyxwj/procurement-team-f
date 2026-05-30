package com.cdp.ecosaas.procurement.auth.infrastructure.persistence.mapper;

import com.cdp.ecosaas.procurement.auth.domain.model.InternalUser;
import com.cdp.ecosaas.procurement.auth.domain.model.UserRole;
import com.cdp.ecosaas.procurement.auth.domain.model.UserStatus;
import com.cdp.ecosaas.procurement.auth.infrastructure.persistence.entity.InternalUserEntity;
import org.mapstruct.*;

/**
 * InternalUser 领域对象与 InternalUserEntity JPA 实体的双向转换 Mapper。
 */
@Mapper(componentModel = "spring")
public interface InternalUserMapper {

    /**
     * 将 JPA 实体转换为领域对象。
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToUserRole")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToUserStatus")
    @Mapping(target = "isSuperAdmin", source = "superAdmin")
    @Mapping(target = "isFirstLogin", source = "firstLogin")
    InternalUser toDomain(InternalUserEntity entity);

    /**
     * 将领域对象转换为 JPA 实体。
     * <p>
     * 审计字段（createdAt, updatedAt, createdBy, updatedBy）和乐观锁版本号
     * 由 JPA 或 Repository 层管理，此处忽略。
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "userRoleToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "userStatusToString")
    @Mapping(target = "superAdmin", source = "superAdmin")
    @Mapping(target = "firstLogin", source = "firstLogin")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    InternalUserEntity toEntity(InternalUser domain);

    /**
     * 将领域对象的字段更新到已有的 JPA 实体上（保留 version、审计字段等）。
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "userRoleToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "userStatusToString")
    @Mapping(target = "superAdmin", source = "superAdmin")
    @Mapping(target = "firstLogin", source = "firstLogin")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(InternalUser domain, @MappingTarget InternalUserEntity entity);

    @Named("stringToUserRole")
    default UserRole stringToUserRole(String role) {
        return role == null ? null : UserRole.valueOf(role);
    }

    @Named("userRoleToString")
    default String userRoleToString(UserRole role) {
        return role == null ? null : role.name();
    }

    @Named("stringToUserStatus")
    default UserStatus stringToUserStatus(String status) {
        return status == null ? null : UserStatus.valueOf(status);
    }

    @Named("userStatusToString")
    default String userStatusToString(UserStatus status) {
        return status == null ? null : status.name();
    }
}
