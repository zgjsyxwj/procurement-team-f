package com.cdp.ecosaas.procurement.supplier.shared.constants;

/**
 * 供应商模块共享常量（任务 17.1）。
 * <p>
 * 集中承载散落在领域服务中的魔法值：供应商编号前缀（Req 6.2）、证件到期提醒节点（Req 12.2）。
 * 证件文件白名单（格式/大小，Req 10.6）按设计已外置为可配置的 {@code OssProperties}
 * （绑定 {@code tencent.oss.*}），为避免双重事实来源，不在此重复声明。
 */
public final class SupplierConstants {

    private SupplierConstants() {
    }

    /** 供应商编号前缀，规则 {@code VD} + 4 位自增序号（Req 6.2）。 */
    public static final String CODE_PREFIX = "VD";

    /** 证件到期提醒节点（距截止日的天数）：30/15/7/3/0（Req 12.2）。 */
    public static final int[] CERT_EXPIRY_REMINDER_NODES = {30, 15, 7, 3, 0};
}
