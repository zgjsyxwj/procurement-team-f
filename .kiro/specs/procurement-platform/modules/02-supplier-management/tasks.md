# 实施计划：供应商管理模块

## 概述

基于 DDD 分层架构，按照数据库 → 领域层 → 基础设施层 → 应用层 → 接口层 → 前端的顺序逐步实现供应商管理模块。后端使用 Java 21 + Spring Boot 3.5，数据库 PostgreSQL 16；前端使用 Vue 3 + TypeScript + ant-design-vue。本模块依赖模块 01（账号体系、采购员-供应商管理关系），并被模块 04（询报价）依赖。账号创建/停用、首登流转通过端口与模块 01 集成。

## 任务列表

- [ ] 1. 数据库 Schema 与基础设施搭建
  - [x] 1.1 创建数据库迁移脚本 `V4__init_supplier_tables.sql`
    - 创建 `supplier` 表（含唯一索引 uk_supplier_code，索引 idx_supplier_status / idx_supplier_name）
    - 创建 `supplier_bank_account` 表（索引 idx_bank_account_supplier）
    - 创建 `supplier_contact` 表（部分唯一索引 uk_contact_primary WHERE is_primary）
    - 创建 `supplier_certificate_type` 表（唯一索引 uk_cert_type_name）
    - 创建 `supplier_cert_type_field` 表
    - 创建 `supplier_certificate` 表（索引 idx_certificate_supplier / audit_status / valid_to）
    - 创建 `supplier_change_request` 表（部分唯一索引 uk_change_pending WHERE status='PENDING_REVIEW'）
    - 创建 `supplier_change_field` 表
    - 创建 `supplier_invitation_log` 表
    - 创建 `supplier_cert_reminder_log` 表（唯一索引 uk_cert_reminder_node）
    - _需求: 3, 6.2, 7, 9, 10, 11, 12_

  - [x] 1.2 添加项目依赖与配置
    - 添加 OSS 对象存储 SDK 依赖（按决定延后至 Task 6.1 实现 OssFileStorageAdapter 时添加）
    - 配置 `OssProperties`（endpoint、bucket、accessKey 等）与上传白名单（PDF/JPG/PNG、≤100MB）
    - 启用定时任务 `@EnableScheduling`，配置证件到期提醒执行时间
    - 复用模块 01 邮件配置发送邀请/通知/提醒邮件
    - _需求: 10.3, 10.6, 12_

- [ ] 2. 领域层 - 模型与枚举
  - [x] 2.1 实现枚举
    - `SupplierStatus`：CREATED, PENDING_ENTRY, PENDING_INFO, PENDING_REVIEW, ACTIVE, DISABLED
    - `SupplierCategory`：DOMESTIC, OVERSEAS
    - `ChangeType`：BASIC_INFO, BANK
    - `ChangeSource`：SUPPLIER, BUYER
    - `ChangeRequestStatus`：PENDING_REVIEW, APPROVED, REJECTED, WITHDRAWN
    - `CertificateAuditStatus`：PENDING_REVIEW, APPROVED, REJECTED
    - `CertificateSource`：SUPPLIER_UPLOAD, BUYER_MAINTAIN
    - `CertificateTypeStatus`：ACTIVE, DISABLED
    - `CertExpiryStatus`（派生）：NORMAL, EXPIRING_SOON, EXPIRED
    - _需求: 7.1, 3, 5, 10, 11, 12.6_

  - [x] 2.2 实现 Supplier 聚合根与 SupplierBankAccount
    - 实现字段：id, supplierCode, name, category, status, unifiedSocialCreditCode, legalPerson, registeredDate, registeredCapital, generalTaxpayer, 选填字段, bankAccounts
    - 实现状态流转方法：`invite()`, `onFirstLogin()`, `submitForReview()`, `approve()`, `reject()`, `activate()`, `disable()`, `enable()`，非法流转抛 `InvalidSupplierStatusException`
    - 实现 `canQuote()`：仅「合作中」可参与报价
    - 实现银行信息完整性约束（填写则户名/银行/账号三项必填）
    - _需求: 3.1, 3.2, 3.9, 4.5, 4.6, 7.1-7.11_

  - [x] 2.3 实现 SupplierContact 实体
    - 实现字段：name, phone, email, isPrimary, position, department
    - 实现 `markPrimary()`：设为主要联系人
    - _需求: 9.1, 9.4_

  - [x] 2.4 实现 SupplierCertificate 实体
    - 实现字段：certTypeId, fileUrl, fileName, validFrom, validTo, auditStatus, source, isCurrentValid, extraFields
    - 实现 `approve()` / `reject(reason)` 审核状态流转
    - 实现 `expiryStatus(today)`：派生正常/即将到期/已过期
    - 校验有效期截止晚于起始
    - _需求: 10.1, 10.2, 10.4, 10.7, 10.8, 10.11, 12.6_

  - [x] 2.5 实现 CertificateType 与 CertTypeField
    - `CertificateType`：name, status, fields；停用保留历史
    - `CertTypeField`：fieldKey, fieldLabel, fieldType, required, sortOrder
    - _需求: 11.1, 11.4, 11.5_

  - [x] 2.6 实现 SupplierChangeRequest 与 SupplierChangeField
    - `SupplierChangeRequest`：supplierId, changeType, source, status, submitter, reviewer, reviewComment，方法 `approve(reviewer)` / `reject(reviewer, reason)` / `withdraw()`
    - `SupplierChangeField`：fieldKey, fieldLabel, beforeValue, afterValue
    - _需求: 3.3, 3.5, 3.7, 5.3, 5.4, 49.3, 50.2_

- [ ] 3. 领域层 - 领域服务与接口
  - [x] 3.1 实现 SupplierLifecycleService（状态机）
    - 集中校验并执行供应商状态合法流转
    - 停用/启用时联动模块 01 账号（通过端口）
    - _需求: 7.2-7.11_

  - [x] 3.2* 编写 SupplierLifecycleService 单元测试
    - 测试各合法流转与非法流转拒绝
    - _需求: 7_

  - [x] 3.3 实现 ChangeReviewService（变更审核）
    - 计算字段级前后差异、生成变更明细
    - 校验同类变更是否已有待审核（冲突拒绝）
    - 审核通过时将变更应用到 supplier 主表
    - _需求: 3.3, 3.6, 5.2, 5.3_

  - [x] 3.4* 编写 ChangeReviewService 单元测试
    - 测试差异计算、同类冲突拦截、通过后应用
    - _需求: 3.3, 3.6, 5.3_

  - [x] 3.5 实现 ContactDomainService
    - 保证每个供应商至少一个主要联系人、设主自动取消原主、不可删唯一主要联系人
    - _需求: 9.3, 9.4, 9.5_

  - [x] 3.6 实现 CertExpiryDomainService 与 SupplierCodeGenerator
    - 到期剩余天数与到期状态计算；命中 {30,15,7,3,0} 提醒节点判断
    - 供应商编号生成：VD + 4 位自增序号
    - _需求: 6.2, 12.2, 12.6_

  - [x] 3.7 定义领域仓储接口
    - `SupplierRepository`、`SupplierContactRepository`、`SupplierCertificateRepository`、`CertificateTypeRepository`、`SupplierChangeRequestRepository`
    - _需求: 3, 8, 9, 10, 50_

  - [x] 3.8 定义领域端口接口
    - `SupplierAccountPort`：创建/停用/启用供应商账号（模块 01）
    - `BuyerSupplierRelationPort`：建立/查询采购员-供应商管理关系（模块 01）
    - `FileStoragePort`：OSS 上传/下载
    - `EmailPort`：邀请、变更待审核/审核结果、证件到期、证件驳回通知
    - _需求: 3.4, 5.4, 6.2, 6.4, 9.7, 10.3, 10.8, 12.2_

- [x] 4. 检查点 - 领域层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 5. 基础设施层 - 持久化
  - [x] 5.1 实现 JPA 实体映射
    - 创建 `SupplierEntity`、`SupplierBankAccountEntity`、`SupplierContactEntity`、`SupplierCertificateEntity`、`CertificateTypeEntity`、`CertTypeFieldEntity`、`SupplierChangeRequestEntity`、`SupplierChangeFieldEntity`、`SupplierInvitationLogEntity`、`CertReminderLogEntity`
    - `supplier` 含乐观锁 `@Version`，`extra_fields` 映射 JSONB
    - _需求: 3, 9, 10, 50_

  - [x] 5.2 实现领域对象与实体的 Mapper
    - `SupplierMapper`、`SupplierContactMapper`、`SupplierCertificateMapper`、`CertificateTypeMapper`（含变更/银行子对象转换）
    - _需求: 3, 9, 10_

  - [x] 5.3 实现 JPA Repository
    - 实现各仓储接口；供应商列表支持名称模糊、状态、证件到期状态筛选与分页
    - 变更记录支持按供应商、时间范围、状态查询；待审核变更与超时（24h）查询
    - _需求: 3.6, 5.1, 5.8, 8.1-8.5, 50.2, 50.3_

- [ ] 6. 基础设施层 - 外部适配器与定时任务
  - [x] 6.1 实现 OssFileStorageAdapter（FileStoragePort）
    - 实现证件文件上传至 OSS、生成下载访问地址
    - 文件格式与大小白名单校验
    - _需求: 10.3, 10.6_
    - ✅ 本轮范围：白名单校验（格式/大小，Req 10.6）已实现并 TDD（7 用例）；腾讯 COS 上传/下载客户端留空（抛 UnsupportedOperationException），未加 cos_api SDK，待真实接入补齐。

  - [x] 6.2 实现 SupplierAccountAdapter 与 BuyerSupplierRelationAdapter
    - `SupplierAccountAdapter`：调用模块 01 创建供应商账号（手机号+初始密码）、停用/启用账号
    - `BuyerSupplierRelationAdapter`：建立（source=CREATED）与查询管理关系
    - _需求: 6.2, 6.4, 7.7, 7.11_
    - ✅ BuyerSupplierRelationAdapter 复用模块 01 `JpaBuyerSupplierRelationRepository` 已实现并 TDD（3 用例）。⏸ SupplierAccountAdapter 仅留空桩（模块 01 无开通服务），账号联动按决策延后至任务 17.2。

  - [x] 6.3 实现 EmailServiceAdapter（EmailPort）
    - 邀请邮件、变更待审核通知、审核通过/驳回通知、证件驳回通知、证件到期提醒
    - _需求: 3.4, 5.4, 6.3, 9.7, 10.8, 12.2, 12.4_
    - ✅ 决策（2026-05-30）：占位实现——所有发信方法直接成功返回（仅记日志、不接真实 SMTP），不抛异常、不阻塞上层流程；待接真实邮件时参照模块 01 EmailServiceAdapter 补齐。

  - [ ] 6.4 实现证件到期提醒定时任务
    - `CertificateExpiryScheduler`（@Scheduled 每日触发）+ `CertificateExpiryJob`
    - 扫描已通过且当前有效证件，命中提醒节点发送邮件，按 `supplier_cert_reminder_log` 去重
    - _需求: 12.1, 12.2, 12.3, 12.4_
    - ⏸ 邮件已决策为占位成功返回；6.4/6.5 是否现在做待用户确认（命中/去重核心逻辑已在 `CertExpiryDomainService` 实现并测试）。

  - [ ] 6.5* 编写到期提醒任务测试
    - 测试节点命中、去重、收件人（供应商+关联采购员）
    - _需求: 12.2, 12.3_
    - ⏸ 随 6.4 一并暂缓。

- [ ] 7. 检查点 - 基础设施层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 8. 应用层 - 命令与查询
  - [x] 8.1 实现供应商创建与邀请 Command 与 Handler
    - `CreateSupplierCommand`、`InviteSupplierCommand`
    - `SupplierCommandHandler`：生成编号 → 保存供应商+主要联系人 → 建号 → 建管理关系 → （保存并邀请时）发邀请邮件+邀请日志+状态流转
    - 校验手机号/邮箱格式，手机号不可重复创建供应商账号
    - _需求: 6.1-6.8, 6.9_
    - ✅ 创建 + 创建时邀请 已 TDD（4 用例）：编号→存供应商(创建成功)+主要联系人→建号(端口,返初始密码)→建关系(source=CREATED)→（邀请时）邮件+邀请日志(SUCCESS/FAILURE)+流转「待进入」；手机号/邮箱格式校验（宽松正则，兼容国外）。新增邀请日志持久化栈（model/repo/dao/jpa 内联映射）。手机号唯一由模块01 createAccount 在 17.2 接通时强制。
    - ⏸ 独立 `InviteSupplierCommand`「重发邀请」按决策暂缓（初始密码只存哈希、明文未留存，`SupplierAccountPort` 无重置口子）。

  - [x] 8.2 实现信息编辑与变更审核 Command 与 Handler
    - `UpdateSupplierInfoCommand`（采购员直接编辑→即时生效+记录变更）
    - `SubmitSupplierChangeCommand`（供应商提交→待审核）、`WithdrawChangeCommand`、`SubmitForReviewCommand`（提交准入审核）、`ReviewChangeCommand`（通过/驳回）
    - `SupplierChangeCommandHandler`：区分入驻草稿与合作中变更两种模式
    - _需求: 3.3, 3.6, 3.7, 4.4, 5.3-5.7, 49.1-49.4_
    - ✅ BASIC_INFO 全套已 TDD（8 用例）：采购员直接编辑(即时生效+已通过记录)、供应商提交(待完善→草稿直接生效 / 合作中→待审核+同类冲突)、撤回、提交准入审核、审核通过(重建应用)/驳回(通知主要联系人)。新增 `SupplierBasicInfoFields` 字段注册表、`Supplier @Builder(toBuilder=true)`、`SupplierNotFoundException`。
    - ⏸ BANK 银行多值变更按决策随后做；合作中提交时「通知关联采购员」(Req 3.4) 暂缓（缺采购员ID→邮箱解析端口，当前仅记日志）。

  - [x] 8.3* 编写变更与审核 Handler 单元测试
    - 测试供应商提交待审核、同类冲突、撤回、采购员直接编辑即时生效、审核通过/驳回
    - _需求: 3.3, 3.6, 3.7, 5.3, 5.4, 49.2_
    - ✅ 由 `SupplierChangeCommandHandlerTests`（8 用例）覆盖（随 8.2 一并完成）。

  - [ ] 8.4 实现供应商状态管理 Command 与 Handler
    - `ChangeSupplierStatusCommand`：状态机流转 + 同步账号停用/启用 + 记录操作备注
    - 停用前受影响事项查询（未完成 RFQ/合同/签署/履约）与风险提示
    - _需求: 7.7-7.12_

  - [ ] 8.5 实现联系人 Command 与 Handler
    - `SaveContactCommand`、`DeleteContactCommand`、`SetPrimaryContactCommand`、`InviteContactCommand`
    - `ContactCommandHandler`：主要联系人约束、采购员编辑即时生效、邀请记录
    - _需求: 9.1-9.10_

  - [ ] 8.6 实现证件 Command 与 Handler
    - `UploadCertificateCommand`（供应商上传→待审核）、`ReviewCertificateCommand`（通过/驳回）、`BuyerAddCertificateCommand`（采购员手动添加→已通过）
    - `CertificateCommandHandler`：OSS 上传、动态字段、当前有效/历史版本
    - _需求: 10.1-10.11_

  - [ ] 8.7 实现证件类型字典 Command 与 Handler
    - `SaveCertTypeCommand`、`UpdateCertTypeFieldsCommand`
    - `CertificateTypeCommandHandler`：名称唯一、停用保留历史、差异化字段维护
    - _需求: 11.1-11.5_

  - [ ] 8.8 实现查询与数据范围服务
    - `SupplierListQuery`、`ChangeHistoryQuery`、`PendingChangeQuery`、`CertificateListQuery`
    - `SupplierQueryHandler` / `SupplierChangeQueryHandler`：列表分页/搜索/筛选+证件到期标注、变更记录、合作中供应商
    - `SupplierAccessService`：按角色与管理关系过滤数据范围（ADMIN 全量 / BUYER 管理范围 / SUPPLIER 本企业）
    - _需求: 2.12, 8, 50.1, 50.4, 50.5_

  - [ ] 8.9 实现首次登录事件监听
    - `SupplierFirstLoginListener`：监听模块 01 首登事件，将「待进入/创建成功」流转为「待完善信息」
    - _需求: 7.3_

- [ ] 9. 接口层 - REST Controller
  - [ ] 9.1 实现 DTO
    - 供应商信息、创建、列表、联系人、证件、变更对比、审核、状态调整、证件类型等请求/响应 DTO
    - _需求: 3, 6, 8, 9, 10, 11, 50_

  - [ ] 9.2 实现 SupplierProfileController（供应商端）
    - `GET/PUT /api/supplier/profile`、`POST /api/supplier/profile/submit-review`
    - `GET/POST /api/supplier/profile/pending-change(/withdraw)`、`GET /api/supplier/cert-types`
    - _需求: 3.1-3.10, 4.1-4.7_

  - [ ] 9.3 实现 SupplierContactController（双端）
    - 供应商端 `/api/supplier/contacts**`、采购端 `/api/suppliers/{id}/contacts**`、联系人邀请
    - _需求: 9.1-9.10_

  - [ ] 9.4 实现 SupplierCertificateController（双端）
    - 供应商端上传/列表、采购端列表/手动添加
    - _需求: 10.1-10.11_

  - [ ] 9.5 实现 SupplierController（采购端）
    - 列表、创建、详情、直接编辑、邀请、停用影响、状态调整、变更记录
    - _需求: 6, 7, 8, 49, 50_

  - [ ] 9.6 实现 SupplierChangeReviewController（审核）
    - 变更待审核列表/详情/通过/驳回、证件审核通过/驳回
    - _需求: 5.1-5.7, 10.7, 10.8_

  - [ ] 9.7 实现 CertificateTypeController（管理端）
    - 证件类型增删改查/停用、差异化字段维护
    - _需求: 11.1-11.6_

  - [ ] 9.8 实现 SupplierInternalController（模块集成）
    - `GET /api/internal/suppliers/active`：合作中供应商列表（供模块 04）
    - _需求: 依赖关系_

  - [ ] 9.9* 编写 Controller 集成测试
    - 测试创建/邀请、变更提交与审核、证件上传与审核、状态调整、权限与数据范围隔离
    - _需求: 5, 6, 7, 8, 10, 50_

- [ ] 10. 检查点 - 后端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 11. 前端 - 基础设施与状态管理
  - [ ] 11.1 实现前端类型定义
    - `supplier.dto.ts`、`contact.dto.ts`、`certificate.dto.ts`、`change.dto.ts`、`supplier-info.vo.ts`、command 类型
    - _需求: 3, 6, 8, 9, 10_

  - [ ] 11.2 实现 service 层（API 调用）
    - `supplier.service.ts`、`supplier-change.service.ts`、`contact.service.ts`、`certificate.service.ts`、`cert-type.service.ts`
    - 复用 `shared/http` axios 实例与 CSRF 适配器
    - _需求: 3, 5, 6, 8, 9, 10, 11, 50_

  - [ ] 11.3 实现 oss-upload.adapter.ts
    - 证件文件上传（格式/大小前端预校验）
    - _需求: 10.1, 10.6_

  - [ ] 11.4 实现 supplier.store.ts（Pinia）
    - 当前供应商信息、列表筛选状态、审核中心待办计数
    - _需求: 3.1, 8_

- [ ] 12. 前端 - 领域与用例层
  - [ ] 12.1 实现前端领域模型
    - `supplier.entity.ts`、`contact.entity.ts`、`certificate.entity.ts`、`supplier-status.vo.ts`、`bank-account.vo.ts`
    - 规则：`bank-account-validation.rule.ts`（填写则三项必填）、`contact-validation.rule.ts`（主要联系人约束）
    - _需求: 3.8, 3.9, 9.3-9.5_

  - [ ] 12.2 实现前端用例层
    - manage-supplier-info、submit-info-change、create-supplier、manage-suppliers、review-change、manage-contacts、manage-certificates、manage-cert-types
    - _需求: 3, 5, 6, 7, 8, 9, 10, 11_

- [ ] 13. 前端 - 供应商端页面与组件
  - [ ] 13.1 实现 SupplierProfileView（默认首页）
    - 企业基本信息表单 + 银行信息（多组）+ 联系人 + 证件 一体化维护流程
    - 引导「待进入/待完善信息」完善并提交准入审核
    - 客户端校验（必填、注册资金正数、手机号/邮箱格式、银行三项联动）
    - 待审核变更标记与撤回入口；不展示变更记录入口
    - _需求: 3.1-3.10, 4.1-4.7, 50.4_

  - [ ] 13.2 实现联系人组件（ContactList / ContactFormDialog）
    - 新增/编辑/删除、设主要联系人、不可删唯一主要联系人
    - _需求: 9.1-9.5_

  - [ ] 13.3 实现证件组件（CertificateUpload / CertificateList）
    - 选择证件类型动态渲染差异化字段、上传、有效期、审核状态展示
    - _需求: 10.1, 10.2, 10.5, 11.6_

  - [ ] 13.4 接入首次登录建议改密
    - 复用模块 01 ChangePasswordDialog，初始密码首登后弹窗建议修改（可跳过）
    - _需求: 4.8_

- [ ] 14. 前端 - 采购/管理端页面与组件
  - [ ] 14.1 实现 SupplierListView
    - 列表字段（企业名称、统一社会信用代码、状态、主要联系人、电话、证件到期状态）
    - 名称模糊搜索、状态筛选、证件到期筛选、分页、证件到期标注（SupplierStatusTag / CertExpiryTag）
    - _需求: 8.1-8.6, 12.5_

  - [ ] 14.2 实现 SupplierCreateView
    - 创建表单（企业名称、分类、主要联系人）+「保存」/「保存并发送邀请」
    - _需求: 6.1-6.3_

  - [ ] 14.3 实现 SupplierDetailView
    - 信息 / 联系人 / 证件 / 变更记录 Tab；采购员直接编辑、手动添加证件
    - 状态调整（StatusChangeDialog，含停用风险提示与受影响事项）、发送/重发邀请（InviteDialog）
    - 变更记录时间倒序 + 时间范围筛选
    - _需求: 7.7-7.12, 9.6-9.8, 10.9, 49, 50.1-50.3_

  - [ ] 14.4 实现 ChangeReviewView（审核中心）
    - 待审核变更列表、前后对比（ChangeDiffView）、通过/驳回（驳回填原因）
    - 证件审核通过/驳回
    - _需求: 5.1-5.7, 10.7, 10.8_

  - [ ] 14.5 实现 CertTypeManagementView（管理端）
    - 证件类型增删改/停用、名称唯一校验、差异化字段维护
    - _需求: 11.1-11.5_

- [ ] 15. 前端 - 路由与权限守卫
  - [ ] 15.1 实现 supplier.routes.ts
    - 供应商端（SUPPLIER）、采购端（BUYER/ADMIN）、管理端（ADMIN）路由与角色守卫
    - 供应商端默认进入企业信息页，无工作台首页
    - _需求: 4.1, 2.8, 2.12_

  - [ ] 15.2 配置角色菜单
    - 在 `frontend/src/config/menu.ts` 按角色加入供应商管理 / 审核中心 / 证件类型字典入口
    - _需求: 2.8, 2.12_

- [ ] 16. 检查点 - 前端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 17. 集成与端到端验证
  - [ ] 17.1 实现共享常量与异常处理
    - `SupplierConstants`（编号前缀、文件白名单、提醒节点）
    - 自定义异常：`SupplierNotFoundException`、`InvalidSupplierStatusException`、`DuplicatePendingChangeException`、`PrimaryContactRequiredException`、`InvalidCertificateFileException`，接入 `GlobalExceptionHandler`
    - _需求: 3.6, 6.5, 7, 9.5, 10.6_

  - [ ] 17.2 跨模块集成联调
    - 账号创建/停用/启用与模块 01 联动、供应商首登状态流转、模块 04 合作中供应商列表
    - _需求: 6.2, 7.3, 7.11, 依赖关系_

  - [ ] 17.3 前后端联调验证
    - 全流程：创建→邀请→首登→完善信息→提交审核→审核通过→合作中
    - 证件上传→审核、信息变更提交→审核/驳回/撤回、采购员直接编辑、状态停用/启用、证件到期提醒
    - _需求: 4, 5, 6, 7, 10, 12_

  - [ ] 17.4* 编写端到端集成测试
    - 入驻全流程、变更审核闭环、证件到期提醒、权限与数据范围隔离
    - _需求: 4, 5, 7, 8, 12, 50_

- [ ] 18. 最终检查点 - 全模块完成
  - 确保所有测试通过，如有疑问请向用户确认。

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加速 MVP 交付
- 每个任务引用了对应的需求编号以确保可追溯性
- 检查点任务确保增量验证
- 后端与前端可并行开发，但联调需在双方基础功能完成后进行
- 本模块依赖模块 01：账号创建/停用、采购员-供应商管理关系、供应商首登事件，均通过端口/事件解耦
- 校验逻辑（必填、注册资金正数、银行三项联动、文件格式/大小、有效期区间）前后端均需实现，后端为权威校验
- 证件差异化字段（Req 11.5）MVP 由本模块承载，后续可对接模块 07 统一表单引擎（Req 13）
