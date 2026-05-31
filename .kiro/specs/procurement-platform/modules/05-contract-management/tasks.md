# 实施计划：合同管理模块

## 概述

基于 DDD 分层架构，按照数据库 → 领域层 → 基础设施层 → 应用层 → 接口层 → 前端的顺序逐步实现合同管理模块。后端使用 Java 21 + Spring Boot 3.5，数据库 PostgreSQL 16；前端使用 Vue 3 + TypeScript + ant-design-vue。

本模块依赖模块 01（账号体系、数据范围）、模块 03（PR/PR 合集）、模块 04（已核价 RFQ 结果），被模块 06（PO/付款）依赖。

**外部依赖（用户决策 2026-05-30）：Worklife BPM、电子签平台、OSS 全部采用空实现（stub）**——`WorklifeBpmAdapter` 推送后直接返回审批通过（BPM 直接审批通过，合同同一请求内流转至「待签署」）、`ESignPlatformAdapter` 创建后直接返回签署完成、`OssFileStorageAdapter` 返回占位对象标识；保留端口抽象与 `/api/external/**` 回调接口供后续真实对接。

**跨模块依赖现状：** 模块 02（供应商）已实现，银行账号回写可走真实集成；模块 03/04/06 尚未实现，其适配器（`RfqResultAdapter`、`PrContractStatusAdapter`、模块 06 集成）本阶段先以桩/占位实现，待对应模块就绪后联调（见 Phase 17）。

## 任务列表

- [ ] 1. 数据库 Schema 与基础设施搭建
  - [ ] 1.1 创建数据库迁移脚本 `V5__init_contract_tables.sql`
    - 创建 `contract` 表（唯一索引 uk_contract_code，索引 idx_contract_pr / bundle / rfq / supplier / status）
    - 创建 `contract_line_item` 表（索引 idx_line_item_contract）
    - 创建 `contract_bank_account` 表（索引 idx_contract_bank_contract）
    - 创建 `contract_approval_log` 表（索引 idx_approval_log_contract）
    - 创建 `contract_operation_log` 表（索引 idx_operation_log_contract）
    - _需求: 27, 28.7, 29.6, 30.3, 47_

  - [ ] 1.2 添加项目配置
    - 配置 `ExternalApprovalProperties`（BPM/电子签端点占位配置）
    - 复用模块 02 OSS 上传白名单约定（PDF/常见文档与图片，拒绝可执行/脚本/含宏）
    - _需求: 27.3, 29.5_

- [ ] 2. 领域层 - 模型与枚举
  - [ ] 2.1 实现枚举
    - `ContractStatus`：DRAFT, EXTERNAL_APPROVAL_PUSHING, WORKLIFE_BPM_APPROVING, REJECTED, PENDING_SIGN, E_SIGNING, COMPLETED, CANCELLED
    - `AgreementType`：FRAMEWORK, NON_FRAMEWORK
    - `SignMethod`：E_SIGN, OFFLINE
    - `ApprovalResult`：APPROVED, REJECTED
    - `ESignStatus`：CREATED, SIGNING, COMPLETED, CANCELLED
    - `PrContractStatus`（汇总派生，不落库）：NOT_CREATED, PENDING_CONTRACT, IN_PROGRESS, COMPLETED
    - _需求: 27.3, 28, 29, 32_

  - [ ] 2.2 实现 Contract 聚合根与子对象
    - `Contract`：基本字段、PR/RFQ 关联、金额/币种、预算快照、签署方式、TNC、CDP 抬头、调整说明、审批/签署/取消信息
    - `ContractLineItem`（中标明细快照，行级币种）、`ContractBankAccount`（银行快照 + syncedToSupplier）
    - `ApprovalRecord`（外部审批单号/结果/回调时间/驳回原因）、`SignatureInfo`（电子签/线下双通道字段）
    - 状态流转方法：`submitForApproval()`, `onBpmReceived()`, `onApprovalCallback()`, `chooseSignMethod()`, `startESign()`, `completeESign()`, `archiveOffline()`, `cancel()`，非法流转抛 `InvalidContractStatusException`
    - `canStartPo()`：仅 COMPLETED 且非 CANCELLED 可发起 PO
    - _需求: 27.3, 27.7, 27.10-27.12, 27.15, 28, 29, 47_

- [ ] 3. 领域层 - 领域服务与接口
  - [ ] 3.1 实现 ContractLifecycleService（状态机）
    - 集中校验并执行合同状态合法流转；状态变化触发 PR 合同状态回写（经端口）
    - _需求: 28, 29, 32, 47_

  - [ ] 3.2* 编写 ContractLifecycleService 单元测试
    - 测试各合法流转（含空实现下 BPM 直接通过的连续流转）与非法流转拒绝
    - _需求: 28, 29_

  - [ ] 3.3 实现 ContractCreationService（带出/金额风险/快照）
    - 校验所选 RFQ 已完成核价审批（或已完成代录报价归档），否则抛 `RfqNotPriceReviewedException`
    - 带出供应商、报价明细、核价结果；构建明细/银行/预算快照
    - 金额风险：超 PR/PR 合集预算→`over_budget` 风险标记（不阻断）；与中标明细合计不一致→要求调整说明
    - 框架合同默认总金额 = 关联 PR/PR 合集预算
    - _需求: 27.1, 27.2, 27.5-27.8, 27.11_

  - [ ] 3.4* 编写 ContractCreationService 单元测试
    - 测试 RFQ 未核价拦截、超预算标记、金额不一致需说明、框架合同默认金额
    - _需求: 27.1, 27.5, 27.6, 27.8, 27.11_

  - [ ] 3.5 实现 ContractCancellationService（分状态取消规则）
    - 按当前状态裁决可取消方与前置动作（撤回/终止外部审批、撤销电子签）；COMPLETED 不可取消
    - _需求: 47.1-47.7_

  - [ ] 3.6* 编写 ContractCancellationService 单元测试
    - 测试各状态取消规则、前置动作要求、COMPLETED 拒绝、CANCELLED 阻止建 PO
    - _需求: 47_

  - [ ] 3.7 实现 ContractCodeGenerator
    - 合同编号生成：CA-YYYYMM-5位自增
    - _需求: 27.3_

  - [ ] 3.8 定义领域仓储接口
    - `ContractRepository`（保存/按 ID/列表分页筛选/按 PR 汇总查询）
    - _需求: 30.1, 30.2, 32_

  - [ ] 3.9 定义领域端口接口
    - `ExternalApprovalPort`（推送/撤回/终止审批）、`ESignPort`（创建/撤销签署流程）、`FileStoragePort`（TNC/归档上传下载）
    - `RfqResultPort`（模块04 已核价 RFQ 结果）、`PrContractStatusPort`（模块03 合同状态回写）、`SupplierBankAccountPort`（模块02 银行账号回写）
    - _需求: 27.1, 27.2, 27.10, 28, 29, 32_

- [ ] 4. 检查点 - 领域层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 5. 基础设施层 - 持久化
  - [ ] 5.1 实现 JPA 实体映射
    - `ContractEntity`、`ContractLineItemEntity`、`ContractBankAccountEntity`、`ContractApprovalLogEntity`、`ContractOperationLogEntity`
    - `contract` 含乐观锁 `@Version`
    - _需求: 27, 28.7, 29.6, 30.3, 47_

  - [ ] 5.2 实现领域对象与实体的 Mapper
    - `ContractMapper`（含明细/银行/审批/签署子对象转换）
    - _需求: 27_

  - [ ] 5.3 实现 JPA Repository
    - 实现 `ContractRepository`；列表支持按合同编号/PR号/RFQ号/供应商/状态筛选与分页
    - 按 PR 查询关联合同（供合同状态汇总）
    - _需求: 30.1, 30.2, 32.5_

- [ ] 6. 基础设施层 - 外部适配器（空实现）与跨模块适配器
  - [ ] 6.1 实现 WorklifeBpmAdapter（ExternalApprovalPort，空实现）
    - 推送审批直接返回通过（占位审批单号）、在进程内驱动 `ContractApprovalHandler` 流转至待签署；撤回/终止直接返回成功
    - 保留真实对接形态注释，待接入后补齐
    - _需求: 28.1-28.7, 47.2_

  - [ ] 6.2 实现 ESignPlatformAdapter（ESignPort，空实现）
    - 创建签署流程直接返回签署完成（占位流程编号）；撤销直接返回成功
    - _需求: 29.3, 29.4, 47.4_

  - [ ] 6.3 实现 OssFileStorageAdapter（FileStoragePort，空实现）
    - 上传返回占位对象标识、下载返回占位地址；文件白名单校验（格式/大小，应用层）
    - _需求: 27.3, 29.5, 29.7_

  - [ ] 6.4 实现跨模块适配器
    - `SupplierBankAccountAdapter`（模块02，真实集成）：合同手填账号选择同步时校验重复后回写供应商银行列表
    - `RfqResultAdapter`（模块04，桩）：查询已核价 RFQ 结果——模块04 未实现，先桩/占位，待联调
    - `PrContractStatusAdapter`（模块03，桩）：回写 PR 合同状态——模块03 未实现，先桩/占位，待联调
    - _需求: 27.1, 27.2, 27.10, 32_

- [ ] 7. 检查点 - 基础设施层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 8. 应用层 - 命令与查询
  - [ ] 8.1 实现合同创建 Command 与 Handler
    - `CreateContractCommand`、`UpdateContractDraftCommand`（草稿/驳回后修改）
    - `ContractCommandHandler`：选源校验 → 带出/快照（ContractCreationService）→ 生成编号 → 保存 DRAFT；银行账号同步选项（SupplierBankAccountPort）
    - _需求: 27, 28.6_

  - [ ] 8.2* 编写创建 Handler 单元测试
    - 测试选源带出、快照保存、超预算标记、银行同步选项
    - _需求: 27.1, 27.2, 27.7, 27.10_

  - [ ] 8.3 实现合同审批 Command 与 Handler
    - `SubmitApprovalCommand`、`HandleApprovalCallbackCommand`
    - `ContractApprovalHandler`：发起审批（推送 BPM）→ 空实现下直接通过至待签署 → 记录 `contract_approval_log` → 回写 PR 合同状态；保留回调入口处理通过/驳回
    - _需求: 28.1-28.7, 32.3_

  - [ ] 8.4 实现合同签署 Command 与 Handler
    - `ChooseSignMethodCommand`、`StartESignCommand`、`HandleESignCallbackCommand`、`OfflineArchiveCommand`
    - `ContractSignHandler`：电子签（空实现直接完成）/ 线下归档（上传文件 + 线下信息 → 完成）→ 回写 PR 合同状态=合同完成
    - _需求: 29.1-29.7_

  - [ ] 8.5 实现合同取消 Command 与 Handler
    - `CancelContractCommand`
    - `ContractCancelHandler`：调用 ContractCancellationService 按状态裁决；外部审批/电子签前置动作经端口（空实现直接成功）；回写 PR 合同状态
    - _需求: 47_

  - [ ] 8.6* 编写审批/签署/取消 Handler 单元测试
    - 测试空实现下审批直接通过、电子签直接完成、线下归档完成、各状态取消规则
    - _需求: 28, 29, 47_

  - [ ] 8.7 实现查询、数据范围与 PR 状态汇总
    - `ContractListQuery`、`ContractDetailQuery`、`SupplierContractListQuery`、`CreatableSourcesQuery`、`PrContractStatusQuery`
    - `ContractQueryHandler`：列表分页/筛选、详情（含外部流程状态/明细/操作记录）、供应商端列表、可选 PR/RFQ
    - `ContractAccessService`：按角色/创建人/PR申请人/供应商过滤数据范围
    - PR 合同状态汇总规则（NOT_CREATED/PENDING_CONTRACT/IN_PROGRESS/COMPLETED）
    - _需求: 27.1, 30.1-30.4, 30.7, 31.1-31.5, 32_

  - [ ] 8.8* 编写查询与数据范围单元测试
    - 测试数据范围裁剪、PR 合同状态汇总（多合同、含取消）
    - _需求: 30.4, 32.4, 32.5_

- [ ] 9. 接口层 - REST Controller
  - 角色门禁：模块01 `SecurityConfig` 加路径规则——`/api/contracts/**` 限 BUYER/ADMIN；`/api/supplier/contracts/**` 限 SUPPLIER；`/api/external/**`、`/api/internal/**` 按外部签名/内部调用约定放行（不走用户 JWT）。控制器为薄控制器。
  - [ ] 9.1 实现 DTO
    - 创建、列表、详情、可选源、发起审批、审批回调、签署方式、电子签、线下归档、取消、电子签回调等请求/响应 DTO
    - _需求: 27, 28, 29, 30, 31, 47_

  - [ ] 9.2 实现 ContractController（采购端）
    - `GET /api/contracts/creatable-sources`、`POST/GET /api/contracts`、`GET/PUT /api/contracts/{id}`
    - `POST /api/contracts/{id}/submit-approval`、`POST /api/contracts/{id}/cancel`、`GET /api/contracts/{id}/archive-file`
    - _需求: 27, 28.1-28.3, 30.1-30.3, 30.7, 47_

  - [ ] 9.3 实现 ContractSignController（采购端）
    - `POST /api/contracts/{id}/sign-method`、`/e-sign`、`/offline-archive`
    - _需求: 29.2, 29.3, 29.5, 29.6_

  - [ ] 9.4 实现 SupplierContractController（供应商端）
    - `GET /api/supplier/contracts`、`GET /api/supplier/contracts/{id}`（电子签入口/链接状态、线下归档结果与下载）
    - _需求: 31.1-31.5_

  - [ ] 9.5 实现 ExternalCallbackController（回调入站，预留）
    - `POST /api/external/contract-approval/callback`、`POST /api/external/e-sign/callback`
    - 空实现下不被外部调用，按真实对接形态预留（签名校验）
    - _需求: 28.5, 28.6, 28.7, 29.4_

  - [ ] 9.6 实现 ContractInternalController（模块集成）
    - `GET /api/internal/contracts/pr-status`（供模块03 汇总展示）、`GET /api/internal/contracts/{id}/po-eligibility`（供模块06）
    - _需求: 32, 47.7_

  - [ ] 9.7* 编写 Controller 集成测试
    - 测试创建、发起审批（直通待签署）、签署/归档、取消、权限与数据范围隔离
    - _需求: 27, 28, 29, 30, 47_

- [ ] 10. 检查点 - 后端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 11. 前端 - 基础设施与状态管理
  - [ ] 11.1 实现前端类型定义
    - `contract.dto.ts`、`sign.dto.ts`、`contract-info.vo.ts`（枚举 + 中文标签/颜色映射）、command 类型
    - _需求: 27, 28, 29, 30, 31_

  - [ ] 11.2 实现 service 层（API 调用）
    - `contract.service.ts`、`supplier-contract.service.ts`，复用 `shared/http` axios 实例与 CSRF 适配器
    - _需求: 27, 28, 29, 30, 31, 47_

  - [ ] 11.3 实现 oss-upload.adapter.ts
    - TNC/归档文件上传（格式/大小前端预校验）
    - _需求: 27.3, 29.5_

  - [ ] 11.4 实现 contract.store.ts（Pinia）
    - 列表筛选状态、当前合同详情、创建向导临时状态
    - _需求: 30_

- [ ] 12. 前端 - 领域与用例层
  - [ ] 12.1 实现前端领域模型与规则
    - `contract.entity.ts`、`contract-status.vo.ts`、`amount-risk.rule.ts`（超预算/与明细不一致）
    - _需求: 27.6, 27.11_

  - [ ] 12.2 实现前端用例层
    - create-contract、manage-contracts、submit-approval、sign-contract、cancel-contract、supplier-contracts
    - _需求: 27, 28, 29, 30, 31, 47_

- [ ] 13. 前端 - 采购/管理端页面与组件
  - [ ] 13.1 实现 ContractCreateView
    - 选源（ContractSourceSelector：PR/PR合集 + 已核价 RFQ）→ 带出供应商/明细/核价 → 填写表单
    - 多币种强提示与按币种拆分建议；超预算红色提醒（AmountRiskAlert）；银行账号「同步到供应商」选项；金额不一致需调整说明
    - _需求: 27.1-27.15_

  - [ ] 13.2 实现 ContractListView
    - 列表字段（合同编号/名称/关联PR/关联RFQ/中标供应商/金额/货币/状态）
    - 按合同编号/PR号/RFQ号/供应商/状态筛选；不展示「待建合同」虚拟记录
    - _需求: 30.1, 30.2, 30.5_

  - [ ] 13.3 实现 ContractDetailView
    - 信息 / 外部流程状态 / 中标供应商与明细 / 操作记录 Tab
    - 发起审批、选择签署方式（SignMethodDialog）、电子签、线下归档（OfflineArchiveDialog）、取消（CancelContractDialog）、归档文件下载
    - 状态后「?」悬停提示（审批/电子签/线下含义）
    - _需求: 28, 29, 30.3, 30.6, 30.7, 47_

- [ ] 14. 前端 - 供应商端页面
  - [ ] 14.1 实现 SupplierContractListView / SupplierContractDetailView
    - 本企业合同列表（区分待签署/签署中/已签署/完成）；详情含电子签入口/链接状态、线下归档结果与下载
    - 确保仅本企业合同可见
    - _需求: 31.1-31.5_

- [ ] 15. 前端 - 路由与权限守卫
  - [ ] 15.1 实现 contract.routes.ts
    - 采购端（BUYER/ADMIN）、供应商端（SUPPLIER）路由与角色守卫
    - _需求: 30.4, 31.1_

  - [ ] 15.2 配置角色菜单
    - 在 `frontend/src/config/menu.ts` 按角色加入合同管理 / 供应商端合同管理入口（key=路由 path）
    - _需求: 30, 31_

- [ ] 16. 检查点 - 前端完成
  - `npm run build`（vue-tsc -b + vite build）通过；Phase 11-15 完成。

- [ ] 17. 集成与端到端验证
  - [ ] 17.1 实现共享常量与异常处理
    - `ContractConstants`（编号前缀 CA、文件白名单、状态提示文案）
    - 自定义异常：`ContractNotFoundException`、`InvalidContractStatusException`、`RfqNotPriceReviewedException`、`ContractCancellationNotAllowedException`，接入 `GlobalExceptionHandler.resolveHttpStatus`（INVALID_CONTRACT_STATUS/CONTRACT_CANCELLATION_NOT_ALLOWED→409、RFQ_NOT_PRICE_REVIEWED→409、CONTRACT_NOT_FOUND→404）
    - _需求: 27.5, 30.4, 47_

  - [ ] 17.2 跨模块集成联调（待模块 03/04/06 就绪）
    - 模块04：从已核价 RFQ 带出供应商/明细/核价结果（替换 RfqResultAdapter 桩）
    - 模块03：PR 合同状态回写与汇总展示、PR 取消联动合同（替换 PrContractStatusAdapter 桩）
    - 模块06：基于已完成合同发起 PO、CANCELLED 阻止建 PO
    - _需求: 27.1, 27.2, 32, 47.6, 47.7, 依赖关系_

  - [ ] 17.3 前后端联调验证
    - 全流程：创建（选源→带出→填写）→ 发起审批（空实现直通待签署）→ 选择签署方式 → 电子签/线下归档 → 合同完成
    - 取消（各状态规则）、供应商端查看、PR 合同状态同步
    - _需求: 27, 28, 29, 30, 31, 32, 47_

  - [ ] 17.4* 编写端到端集成测试
    - 创建到完成全流程、取消闭环、权限与数据范围隔离、PR 状态汇总
    - _需求: 27, 28, 29, 30, 32, 47_

- [ ] 18. 最终检查点 - 全模块完成
  - 确保所有测试通过，如有疑问请向用户确认。

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加速 MVP 交付
- 每个任务引用了对应的需求编号以确保可追溯性
- 检查点任务确保增量验证
- 后端与前端可并行开发，但联调需在双方基础功能完成后进行
- **外部依赖（Worklife BPM / 电子签 / OSS）本阶段全部空实现**：BPM 直接审批通过、电子签直接完成、OSS 占位标识；端口与 `/api/external/**` 回调接口保留供后续真实对接
- **跨模块依赖**：模块 02 已实现（银行回写可真实集成）；模块 03/04/06 未实现，相关适配器先桩/占位，Phase 17 替换为真实集成
- 金额风险（超预算/与明细不一致）前后端均需实现，后端为权威校验
- TNC 当前仅作合同附件上传，不做模块化条款库（Req 27.13）；字段必填/可编辑规则配置化，默认「可带出则带出、未带出非必填且可编辑」（Req 27.14）
