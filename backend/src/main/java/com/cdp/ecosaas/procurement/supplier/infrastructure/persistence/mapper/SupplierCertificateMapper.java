package com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.mapper;

import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateAuditStatus;
import com.cdp.ecosaas.procurement.supplier.domain.model.CertificateSource;
import com.cdp.ecosaas.procurement.supplier.domain.model.SupplierCertificate;
import com.cdp.ecosaas.procurement.supplier.infrastructure.persistence.entity.SupplierCertificateEntity;
import org.mapstruct.*;

/**
 * SupplierCertificate 领域对象与 SupplierCertificateEntity 的转换 Mapper。
 * <p>
 * 领域布尔字段 {@code isCurrentValid} 对应实体 {@code currentValid} / 列 {@code is_current_valid}。
 */
@Mapper(componentModel = "spring")
public interface SupplierCertificateMapper {

    @Mapping(target = "auditStatus", source = "auditStatus", qualifiedByName = "stringToAuditStatus")
    @Mapping(target = "source", source = "source", qualifiedByName = "stringToCertSource")
    @Mapping(target = "isCurrentValid", source = "currentValid")
    SupplierCertificate toDomain(SupplierCertificateEntity entity);

    @Mapping(target = "auditStatus", source = "auditStatus", qualifiedByName = "auditStatusToString")
    @Mapping(target = "source", source = "source", qualifiedByName = "certSourceToString")
    @Mapping(target = "currentValid", source = "currentValid")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplierCertificateEntity toEntity(SupplierCertificate domain);

    @Mapping(target = "auditStatus", source = "auditStatus", qualifiedByName = "auditStatusToString")
    @Mapping(target = "source", source = "source", qualifiedByName = "certSourceToString")
    @Mapping(target = "currentValid", source = "currentValid")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplierId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(SupplierCertificate domain, @MappingTarget SupplierCertificateEntity entity);

    @Named("stringToAuditStatus")
    default CertificateAuditStatus stringToAuditStatus(String value) {
        return value == null ? null : CertificateAuditStatus.valueOf(value);
    }

    @Named("auditStatusToString")
    default String auditStatusToString(CertificateAuditStatus value) {
        return value == null ? null : value.name();
    }

    @Named("stringToCertSource")
    default CertificateSource stringToCertSource(String value) {
        return value == null ? null : CertificateSource.valueOf(value);
    }

    @Named("certSourceToString")
    default String certSourceToString(CertificateSource value) {
        return value == null ? null : value.name();
    }
}
