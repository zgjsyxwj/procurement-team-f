package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper;

import com.cdp.ecosaas.procurement.supplier.domain.model.Supplier;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierBankAccount;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCategory;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierStatus;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierBankAccountEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * Supplier 领域对象与 SupplierEntity 的转换 Mapper（含银行账号子对象转换）。
 * <p>
 * 银行账号作为聚合子记录由 {@code JpaSupplierRepository} 单独装配，故 {@code toDomain}
 * 以独立参数注入；审计字段与乐观锁版本由 JPA / 仓储层管理，此处忽略。
 */
@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "category", source = "entity.category", qualifiedByName = "stringToCategory")
    @Mapping(target = "status", source = "entity.status", qualifiedByName = "stringToStatus")
    @Mapping(target = "bankAccounts", source = "bankAccounts")
    Supplier toDomain(SupplierEntity entity, List<SupplierBankAccount> bankAccounts);

    @Mapping(target = "category", source = "category", qualifiedByName = "categoryToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    SupplierEntity toEntity(Supplier domain);

    @Mapping(target = "category", source = "category", qualifiedByName = "categoryToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplierCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(Supplier domain, @MappingTarget SupplierEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplierId", ignore = true)
    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplierBankAccountEntity toBankEntity(SupplierBankAccount domain);

    SupplierBankAccount toBankDomain(SupplierBankAccountEntity entity);

    List<SupplierBankAccount> toBankDomains(List<SupplierBankAccountEntity> entities);

    @Named("stringToCategory")
    default SupplierCategory stringToCategory(String value) {
        return value == null ? null : SupplierCategory.valueOf(value);
    }

    @Named("categoryToString")
    default String categoryToString(SupplierCategory value) {
        return value == null ? null : value.name();
    }

    @Named("stringToStatus")
    default SupplierStatus stringToStatus(String value) {
        return value == null ? null : SupplierStatus.valueOf(value);
    }

    @Named("statusToString")
    default String statusToString(SupplierStatus value) {
        return value == null ? null : value.name();
    }
}
