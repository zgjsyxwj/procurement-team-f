package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertTypeField;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateType;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateTypeStatus;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.CertTypeFieldEntity;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.CertificateTypeEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * CertificateType 领域对象与 CertificateTypeEntity 的转换 Mapper（含差异化字段子对象转换）。
 * <p>
 * 差异化字段作为聚合子记录由 {@code JpaCertificateTypeRepository} 单独装配，故 {@code toDomain} 以独立参数注入。
 */
@Mapper(componentModel = "spring")
public interface CertificateTypeMapper {

    @Mapping(target = "status", source = "entity.status", qualifiedByName = "stringToCertTypeStatus")
    @Mapping(target = "fields", source = "fields")
    CertificateType toDomain(CertificateTypeEntity entity, List<CertTypeField> fields);

    @Mapping(target = "status", source = "status", qualifiedByName = "certTypeStatusToString")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CertificateTypeEntity toEntity(CertificateType domain);

    @Mapping(target = "status", source = "status", qualifiedByName = "certTypeStatusToString")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(CertificateType domain, @MappingTarget CertificateTypeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "certTypeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CertTypeFieldEntity toFieldEntity(CertTypeField domain);

    CertTypeField toFieldDomain(CertTypeFieldEntity entity);

    List<CertTypeField> toFieldDomains(List<CertTypeFieldEntity> entities);

    @Named("stringToCertTypeStatus")
    default CertificateTypeStatus stringToCertTypeStatus(String value) {
        return value == null ? null : CertificateTypeStatus.valueOf(value);
    }

    @Named("certTypeStatusToString")
    default String certTypeStatusToString(CertificateTypeStatus value) {
        return value == null ? null : value.name();
    }
}
