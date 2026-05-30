package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper;

import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierContact;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierContactEntity;
import org.mapstruct.*;

/**
 * SupplierContact 领域对象与 SupplierContactEntity 的转换 Mapper。
 * <p>
 * 领域布尔字段 {@code isPrimary} 对应实体 {@code primary} / 列 {@code is_primary}。
 */
@Mapper(componentModel = "spring")
public interface SupplierContactMapper {

    @Mapping(target = "isPrimary", source = "primary")
    SupplierContact toDomain(SupplierContactEntity entity);

    @Mapping(target = "primary", source = "primary")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SupplierContactEntity toEntity(SupplierContact domain);

    @Mapping(target = "primary", source = "primary")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplierId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(SupplierContact domain, @MappingTarget SupplierContactEntity entity);
}
