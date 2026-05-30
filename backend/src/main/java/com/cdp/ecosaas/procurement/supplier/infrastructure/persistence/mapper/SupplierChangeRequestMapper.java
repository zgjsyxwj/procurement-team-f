package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper;

import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeRequestStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.ChangeType;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierChangeRequest;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierChangeFieldEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierChangeRequestEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * SupplierChangeRequest 领域对象与 SupplierChangeRequestEntity 的转换 Mapper（含变更字段子对象转换）。
 * <p>
 * 变更字段作为聚合子记录由 {@code JpaSupplierChangeRequestRepository} 单独装配，故 {@code toDomain} 以独立参数注入。
 */
@Mapper(componentModel = "spring")
public interface SupplierChangeRequestMapper {

    @Mapping(target = "changeType", source = "entity.changeType", qualifiedByName = "stringToChangeType")
    @Mapping(target = "source", source = "entity.source", qualifiedByName = "stringToChangeSource")
    @Mapping(target = "status", source = "entity.status", qualifiedByName = "stringToChangeStatus")
    @Mapping(target = "fields", source = "fields")
    SupplierChangeRequest toDomain(SupplierChangeRequestEntity entity, List<SupplierChangeField> fields);

    @Mapping(target = "changeType", source = "changeType", qualifiedByName = "changeTypeToString")
    @Mapping(target = "source", source = "source", qualifiedByName = "changeSourceToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "changeStatusToString")
    @Mapping(target = "createdAt", ignore = true)
    SupplierChangeRequestEntity toEntity(SupplierChangeRequest domain);

    @Mapping(target = "changeType", source = "changeType", qualifiedByName = "changeTypeToString")
    @Mapping(target = "source", source = "source", qualifiedByName = "changeSourceToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "changeStatusToString")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(SupplierChangeRequest domain, @MappingTarget SupplierChangeRequestEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "changeRequestId", ignore = true)
    SupplierChangeFieldEntity toFieldEntity(SupplierChangeField domain);

    SupplierChangeField toFieldDomain(SupplierChangeFieldEntity entity);

    List<SupplierChangeField> toFieldDomains(List<SupplierChangeFieldEntity> entities);

    @Named("stringToChangeType")
    default ChangeType stringToChangeType(String value) {
        return value == null ? null : ChangeType.valueOf(value);
    }

    @Named("changeTypeToString")
    default String changeTypeToString(ChangeType value) {
        return value == null ? null : value.name();
    }

    @Named("stringToChangeSource")
    default ChangeSource stringToChangeSource(String value) {
        return value == null ? null : ChangeSource.valueOf(value);
    }

    @Named("changeSourceToString")
    default String changeSourceToString(ChangeSource value) {
        return value == null ? null : value.name();
    }

    @Named("stringToChangeStatus")
    default ChangeRequestStatus stringToChangeStatus(String value) {
        return value == null ? null : ChangeRequestStatus.valueOf(value);
    }

    @Named("changeStatusToString")
    default String changeStatusToString(ChangeRequestStatus value) {
        return value == null ? null : value.name();
    }
}
