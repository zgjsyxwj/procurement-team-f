package com.cdp.ecosaas.procurement.supplier.domain.port;

/**
 * 供应商账号端口（出站，对接模块 01 认证权限）。
 * <p>
 * 供应商企业（本模块）与登录账号（模块 01 {@code auth_supplier_user}）分离，账号的创建/停用/启用
 * 通过本端口回调模块 01，保持领域层无跨模块直接依赖。实现见 {@code SupplierAccountAdapter}（任务 6.2）。
 */
public interface SupplierAccountPort {

    /**
     * 以主要联系人手机号为登录凭据创建供应商登录账号，返回生成的初始密码明文（用于邀请邮件，Req 6.2）。
     *
     * @throws com.cdp.ecosaas.procurement.shared.exception.BusinessException 手机号已被占用等（Req 6.5）
     */
    String createAccount(Long supplierId, String contactName, String phone, String email);

    /**
     * 解析供应商登录账号（模块 01 用户 ID）所属的供应商企业 ID（只读，供应商门户端数据范围）。
     *
     * @return 关联的供应商企业 ID；账号不存在或未关联企业时为空
     */
    java.util.Optional<Long> findSupplierIdByUserId(Long userId);

    /**
     * 停用供应商账号，停用后供应商不可登录（Req 7.7、7.11）。
     */
    void disableAccount(Long supplierId);

    /**
     * 启用供应商账号（Req 7.8）。
     */
    void enableAccount(Long supplierId);
}
