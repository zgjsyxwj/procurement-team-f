# 实施计划：履约与付款模块

## 概述

基于 DDD 分层架构，按照数据库 → 领域层 → 基础设施层 → 应用层 → 接口层 → 前端的顺序逐步实现履约与付款模块。后端使用 Java 21 + Spring Boot 3.5，数据库 PostgreSQL 16；前端使用 Vue 3 + TypeScript + ant-design-vue。

本模块依赖模块 01（账号体系、数据范围）、模块 03（PR/PR 原始申请人）、模块 05（已完成合同 + 可发起 PO 校验），是采购全链路终点。

**外部依赖（用户决策 2026-05-30）：Worklife BPM、台账管理系统全部采用空实现（stub）**——`WorklifeBpmAdapter` 推送后直接返回审批通过（占位审批单号，PO 同一请求内流转至审批通过）、`LedgerSystemAdapter` 审批通过后调用同步直接返回接收成功（台账同步状态置已同步、付款状态置待付款），**付款回传不自动推进**（付款状态停留待付款）；保留端口抽象与 `/api/external/**` 回调接口（含付款回调）供后续真实对接。

**跨模块依赖现状：** 模块 03（PR）、模块 05（合同）尚未实现，其适配器（`PrInfoAdapter`、`PrPoStatusAdapter`、`ContractAdapter`）本阶段先以桩/占位实现，待对应模块就绪后联调（见 Phase 17）。

## 任务列表

- [ ] 1. 数据库 Schema 与基础设施搭建
  - [ ] 1.1 创建数据库迁移脚本 `V6__init_po_tables.sql`
    - 创建 `purchase_order` 表（唯一索引 uk_po_code，索引 idx_po_pr / contract / supplier / approval_status / ledger_sync_status / payment_status）
    - 创建 `po_line_item` 表（索引 idx_po_line_item_po）
    - 创建 `po_approval_log` 表（索引 idx_po_approval_log_po）
    - 创建 `po_ledger_sync_log` 表（索引 idx_po_sync_log_po）
    - 创建 `po_payment_record` 表（索引 idx_po_payment_record_po）
    - 创建 `po_operation_log` 表（索引 idx_po_operation_log_po）
    - _需求: 33, 34, 35.2, 48_

  - [ ] 1.2 添加项目配置
    - 配置 `ExternalIntegrationProperties`（BPM/台账端点占位配置）
    - _需求: 33.6, 34.1_

- [ ] 2. 领域层 - 模型与枚举
  - [ ] 2.1 实现枚举
    - `PoApprovalStatus`：DRAFT, APPROVING, APPROVED, REJECTED, CANCELLED
    - `LedgerSyncStatus`：NOT_SYNCED, SYNCED, SYNC_FAILED, CANCELLED
    - `PaymentStatus`：PENDING_PAYMENT, PARTIAL_PAID, FULLY_PAID
    - `PoType`：BUSINESS, EXPENSE
    - `ApprovalResult`：APPROVED, REJECTED
    - _需求: 33, 34, 35.3_

  - [ ] 2.2 实现 PurchaseOrder 聚合根与子对象
    - `PurchaseOrder`：PR/合同/供应商关联与快照、PO 类型、拆分金额/币种、归集对象、销售订单/成本中心、付款条款、超合同标记、三类独立状态、已付款金额、审批/取消信息
    - `PoLineItem`（中标明细快照，行级币种、合同明细溯源）、`ApprovalRecord`（审批单号/结果/意见/完成时间/操作人/驳回原因）、`PaymentRecord`（付款回传记录值对象）
    - 状态流转方法：`submitForApproval()`, `onApprovalCallback()`, `onSyncResult()`, `onPaymentCallback()`, `cancel()`, `confirmCancel()`，非法流转抛 `InvalidPoStatusException`
    - `canSubmitApproval()`（DRAFT/REJECTED）、`canSyncLedger()`（仅 APPROVED 且 NOT_SYNCED）
    - 三类状态互不耦合：审批/同步/付款各自独立流转（Req 33.10、35.3）
    - _需求: 33.2-33.5, 33.8-33.11, 34.3, 34.5, 35.3, 48_

- [ ] 3. 领域层 - 领域服务与接口
  - [ ] 3.1 实现 PoLifecycleService（三套状态机）
    - 集中校验并执行 PO 审批 / 台账同步 / 付款 三套状态的合法流转；状态变化触发 PR 状态回写（经端口）
    - 仅审批通过后允许台账同步（审批中/驳回/撤回不得同步，Req 35.5、52.4）
    - _需求: 33.7, 34.3, 35.3, 35.5, 52.4_

  - [ ] 3.2* 编写 PoLifecycleService 单元测试
    - 测试各合法流转（含空实现下 BPM 直通审批通过、同步直通成功的连续流转）与非法流转拒绝（如未审批通过即同步、付款状态被采购系统直接修改）
    - _需求: 33.7, 34.3, 35.5, 52.4_

  - [ ] 3.3 实现 PoCreationService（带出/拆分/类型校验/超合同）
    - 校验所选合同可发起 PO（已完成且未取消），否则抛 `ContractNotPoEligibleException`
    - 带出合同/供应商/最终中标价/付款条款/明细；构建明细快照与 PR 申请人快照
    - 按 PO 类型校验归集对象必填（业务类→销售订单、费用类→成本中心），否则抛 `PoTypeRequirementException`
    - 拆分金额校验与超合同范围判定（`over_contract` 标记，不阻断）
    - _需求: 33.1-33.5, 33.11, 52.3_

  - [ ] 3.4* 编写 PoCreationService 单元测试
    - 测试合同不可发起拦截、带出快照、业务类缺销售订单/费用类缺成本中心拦截、超合同标记
    - _需求: 33.1, 33.4, 33.5, 52.3_

  - [ ] 3.5 实现 PoCancellationService（分状态取消规则）
    - 按审批状态 + 台账同步/付款状态裁决可取消方与前置动作（BPM 撤回、经理确认）；已同步/已付款不可直接取消
    - _需求: 48.1-48.7_

  - [ ] 3.6* 编写 PoCancellationService 单元测试
    - 测试草稿/驳回直接取消、审批中需撤回、审批通过未同步需确认、已同步拒绝、已付款拒绝
    - _需求: 48_

  - [ ] 3.7 实现 PoCodeGenerator
    - PO 编号生成：PO-YYYYMM-5位自增
    - _需求: 33.2_

  - [ ] 3.8 定义领域仓储接口
    - `PurchaseOrderRepository`（保存/按 ID/列表分页筛选/按 PR 聚合查询）
    - _需求: 35.1, 34.7, 35.4_

  - [ ] 3.9 定义领域端口接口
    - `ExternalApprovalPort`（推送/撤回 PO 审批）、`LedgerSyncPort`（同步 PO 至台账）
    - `ContractPort`（模块05 可发起PO校验/合同带出）、`PrInfoPort`（模块03 PR信息/原始申请人）、`PrPoStatusPort`（模块03 PO/付款状态回写）
    - _需求: 33.1, 33.2, 33.6, 34.1, 34.7, 48.3_

- [ ] 4. 检查点 - 领域层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 5. 基础设施层 - 持久化
  - [ ] 5.1 实现 JPA 实体映射
    - `PurchaseOrderEntity`、`PoLineItemEntity`、`PoApprovalLogEntity`、`PoLedgerSyncLogEntity`、`PoPaymentRecordEntity`、`PoOperationLogEntity`
    - `purchase_order` 含乐观锁 `@Version`
    - _需求: 33, 34, 35.2, 48_

  - [ ] 5.2 实现领域对象与实体的 Mapper
    - `PurchaseOrderMapper`（含明细/审批/付款记录子对象转换）
    - _需求: 33_

  - [ ] 5.3 实现 JPA Repository
    - 实现 `PurchaseOrderRepository`；列表支持按 PO编号/PR/合同/供应商/三类状态筛选与分页
    - 按 PR 聚合查询关联 PO（供 PR 付款汇总）
    - _需求: 35.1, 34.7, 35.4_

- [ ] 6. 基础设施层 - 外部适配器（空实现）与跨模块适配器
  - [ ] 6.1 实现 WorklifeBpmAdapter（ExternalApprovalPort，空实现）
    - 推送审批直接返回通过（占位审批单号），在进程内驱动 `PoApprovalHandler` 流转至审批通过；撤回直接返回成功
    - 保留真实对接形态注释，待接入后补齐
    - _需求: 33.6, 33.9, 48.3_

  - [ ] 6.2 实现 LedgerSystemAdapter（LedgerSyncPort，空实现）
    - 审批通过后调用同步直接返回接收成功，在进程内驱动 `PoSyncHandler` 置已同步/待付款
    - 付款回传不自动推进，保留 `/api/external/ledger/payment/callback` 真实对接形态
    - _需求: 34.1, 34.2, 34.3_

  - [ ] 6.3 实现跨模块适配器（桩）
    - `ContractAdapter`（模块05，桩）：可发起 PO 校验与合同/供应商/中标价/付款条款/明细带出——模块05 未实现，先桩/占位，待联调
    - `PrInfoAdapter`（模块03，桩）：PR 信息与原始申请人——模块03 未实现，先桩/占位
    - `PrPoStatusAdapter`（模块03，桩）：回写 PO/付款状态至 PR——模块03 未实现，先桩/占位
    - _需求: 33.1, 33.2, 34.7, 35.4_

- [ ] 7. 检查点 - 基础设施层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 8. 应用层 - 命令与查询
  - [ ] 8.1 实现 PO 创建 Command 与 Handler
    - `CreatePoCommand`、`UpdatePoDraftCommand`（草稿/驳回后修改）
    - `PoCommandHandler`：可发起合同校验 → 带出/快照/类型校验（PoCreationService）→ 生成编号 → 保存 DRAFT/NOT_SYNCED
    - _需求: 33.1-33.5, 33.8, 33.11_

  - [ ] 8.2* 编写创建 Handler 单元测试
    - 测试带出快照、拆分、类型必填校验、超合同标记
    - _需求: 33.2-33.5, 52.3_

  - [ ] 8.3 实现 PO 审批 Command 与 Handler
    - `SubmitApprovalCommand`、`HandleApprovalCallbackCommand`
    - `PoApprovalHandler`：发起审批（推送 BPM）→ 空实现下直接通过 → 记录 `po_approval_log`（单号/意见/完成时间/操作人）→ 触发台账同步 → 回写 PR 状态；保留回调入口处理通过/驳回
    - _需求: 33.6, 33.7, 33.9, 52.1-52.4_

  - [ ] 8.4 实现台账同步与付款 Command 与 Handler
    - `RetrySyncCommand`、`HandlePaymentCallbackCommand`
    - `PoSyncHandler`：审批通过后同步 PO（编号/PR/合同/供应商/金额/币种/付款条款/类型/成本中心或销售订单/明细）→ 空实现直接成功（已同步/待付款）→ 记录 `po_ledger_sync_log`；同步失败置 SYNC_FAILED 并支持重试；付款回调更新付款状态/已付款金额/付款时间/流水号并记录 `po_payment_record`，回写 PR
    - 台账回传为付款状态与金额唯一可信来源，付款字段不接受其他入口写入
    - _需求: 34.1-34.7_

  - [ ] 8.5 实现 PO 取消 Command 与 Handler
    - `CancelPoCommand`、`ConfirmCancelPoCommand`
    - `PoCancelHandler`：调用 PoCancellationService 按状态裁决；审批中先经 BPM 撤回（端口，空实现直接成功）；审批通过未同步需经理/Admin 确认置审批+同步=已取消；回写 PR
    - _需求: 48.1-48.7_

  - [ ] 8.6* 编写审批/同步/取消 Handler 单元测试
    - 测试空实现下审批直通通过并同步、付款回调推进、各状态取消规则
    - _需求: 33.7, 34, 48_

  - [ ] 8.7 实现查询、数据范围与 PR 付款汇总
    - `PoListQuery`、`PoDetailQuery`、`CreatableContractsQuery`、`PrPoSummaryQuery`
    - `PoQueryHandler`：列表分页/筛选（三类状态独立）、详情（基本/审批/台账同步/付款记录/明细/成本中心或销售订单；不含同步模块说明与状态流程说明）、可发起合同、按 PR 汇总付款
    - `PoAccessService`：按角色/创建人/PR 原始申请人过滤数据范围；发起权限校验 PR 原始申请人
    - _需求: 33.1, 33.10, 34.7, 35.1-35.7_

  - [ ] 8.8* 编写查询与数据范围单元测试
    - 测试数据范围裁剪、发起权限（仅 PR 原始申请人）、三类状态独立返回、PR 付款汇总（多 PO）
    - _需求: 33.1, 33.10, 34.7, 35.3, 35.4_

- [ ] 9. 接口层 - REST Controller
  - 角色门禁：模块01 `SecurityConfig` 加路径规则——`/api/purchase-orders/**` 限 BUSINESS_USER/BUYER/ADMIN（按数据范围）；`/api/external/**`、`/api/internal/**` 按外部签名/内部调用约定放行（不走用户 JWT）。控制器为薄控制器。
  - [ ] 9.1 实现 DTO
    - 创建、列表、详情、可发起合同、发起审批、审批回调、付款回调、取消、PR汇总等请求/响应 DTO
    - 详情响应不含「系统同步模块」与「PO 状态流程」说明字段；PR 号为可点击链接字段
    - _需求: 33, 34, 35, 48_

  - [ ] 9.2 实现 PurchaseOrderController（业务/采购端）
    - `GET /api/purchase-orders/creatable-contracts`、`POST/GET /api/purchase-orders`、`GET/PUT /api/purchase-orders/{id}`
    - `POST /api/purchase-orders/{id}/cancel`、`POST /api/purchase-orders/{id}/confirm-cancel`
    - _需求: 33.1-33.5, 33.8, 35.1, 35.2, 35.6, 35.7, 48_

  - [ ] 9.3 实现 PoApprovalController（业务/采购端）
    - `POST /api/purchase-orders/{id}/submit-approval`、`POST /api/purchase-orders/{id}/retry-sync`
    - _需求: 33.6, 34.4, 52.1-52.3_

  - [ ] 9.4 实现 ExternalCallbackController（回调入站，预留）
    - `POST /api/external/po-approval/callback`、`POST /api/external/ledger/payment/callback`
    - 空实现下不被外部调用，按真实对接形态预留（签名校验）
    - _需求: 33.9, 34.5, 34.6_

  - [ ] 9.5 实现 PoInternalController（模块集成）
    - `GET /api/internal/purchase-orders/pr-summary`（供模块03 PR列表与「PO与付款」Tab 汇总）
    - _需求: 34.7, 35.4_

  - [ ] 9.6* 编写 Controller 集成测试
    - 测试创建、发起审批（直通通过并同步）、重试同步、取消/确认取消、权限与数据范围隔离
    - _需求: 33, 34, 35, 48_

- [ ] 10. 检查点 - 后端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 11. 前端 - 基础设施与状态管理
  - [ ] 11.1 实现前端类型定义
    - `purchase-order.dto.ts`、`po-info.vo.ts`（三类状态枚举 + 中文标签/颜色映射）、command 类型
    - _需求: 33, 34, 35_

  - [ ] 11.2 实现 service 层（API 调用）
    - `purchase-order.service.ts`，复用 `shared/http` axios 实例与 CSRF 适配器
    - _需求: 33, 34, 35, 48_

  - [ ] 11.3 实现 order.store.ts（Pinia）
    - 列表筛选状态、当前 PO 详情、创建向导临时状态
    - _需求: 35_

- [ ] 12. 前端 - 领域与用例层
  - [ ] 12.1 实现前端领域模型与规则
    - `purchase-order.entity.ts`、`po-status.vo.ts`、`over-contract.rule.ts`（超合同范围预提示）
    - _需求: 35.3, 52.3_

  - [ ] 12.2 实现前端用例层
    - create-po、manage-pos、submit-po-approval（超合同风险提示）、cancel-po、pr-po-summary
    - _需求: 33, 34, 35, 48, 52.3_

- [ ] 13. 前端 - 页面与组件
  - [ ] 13.1 实现 PoCreateView
    - 选可发起合同（CreatableContractSelector）→ 带出供应商/中标价/付款条款/明细 → 填写（PoBasicForm：类型/拆分金额/归集对象/销售订单或成本中心）
    - 业务类必填销售订单、费用类必填成本中心；超合同范围红色提示（OverContractAlert）
    - _需求: 33.1-33.5, 52.3_

  - [ ] 13.2 实现 PoListView
    - 列表字段（PO编号/关联PR/关联合同/供应商/PO金额/货币/PO审批状态/台账同步状态/付款状态/已付款金额）
    - 三类状态独立标签（PoStatusTags）；按 PO编号/PR/合同/供应商/状态筛选
    - _需求: 35.1, 35.3_

  - [ ] 13.3 实现 PoDetailView
    - 基本信息 / 审批信息 / 台账同步结果 / 付款回传记录（PaymentRecordTable）/ PO 明细 / 成本中心或销售订单 Tab
    - 发起审批（超合同提示）、重试同步、取消（CancelPoDialog）、确认取消
    - 三类状态独立展示；不展示「系统同步模块」说明卡与「PO 状态流程」说明卡；关联 PR 号可点击跳转 PR 详情
    - _需求: 33.6, 34.4, 35.2, 35.3, 35.6, 35.7, 48_

  - [ ] 13.4 实现 PrPoSummaryPanel（供模块03 复用）
    - PR 详情「PO与付款」Tab：汇总展示该 PR 下多个 PO 的三类状态与付款结果
    - 以独立组件导出，供模块03 PR 详情页内嵌
    - _需求: 34.7, 35.4_

- [ ] 14. 前端 - 路由与权限守卫
  - [ ] 14.1 实现 order.routes.ts
    - 业务/采购/管理端（BUSINESS_USER/BUYER/ADMIN）路由与角色守卫
    - _需求: 35_

  - [ ] 14.2 配置角色菜单
    - 在 `frontend/src/config/menu.ts` 按角色加入 PO/付款管理入口（key=路由 path）
    - _需求: 35_

- [ ] 15. 检查点 - 前端完成
  - `npm run build`（vue-tsc -b + vite build）通过；Phase 11-14 完成。

- [ ] 16. 集成与端到端验证
  - [ ] 16.1 实现共享常量与异常处理
    - `OrderConstants`（编号前缀 PO、状态提示文案）
    - 自定义异常：`PurchaseOrderNotFoundException`、`InvalidPoStatusException`、`ContractNotPoEligibleException`、`PoTypeRequirementException`、`PoCancellationNotAllowedException`，接入 `GlobalExceptionHandler.resolveHttpStatus`（INVALID_PO_STATUS/PO_CANCELLATION_NOT_ALLOWED→409、CONTRACT_NOT_PO_ELIGIBLE→409、PO_TYPE_REQUIREMENT→400、PURCHASE_ORDER_NOT_FOUND→404）
    - _需求: 33.1, 33.4, 33.5, 48_

  - [ ] 16.2 跨模块集成联调（待模块 03/05 就绪）
    - 模块05：从已完成合同带出供应商/中标价/付款条款/明细，可发起 PO 校验（替换 ContractAdapter 桩）
    - 模块03：PR 信息与原始申请人、PO/付款状态回写与 PR 列表/「PO与付款」Tab 汇总（替换 PrInfoAdapter / PrPoStatusAdapter 桩）
    - _需求: 33.1, 33.2, 34.7, 35.4, 依赖关系_

  - [ ] 16.3 前后端联调验证
    - 全流程：选可发起合同 → 带出/拆分填写 → 发起审批（空实现直通通过并同步台账→已同步/待付款）→ 列表/详情三类状态独立展示
    - 取消（各状态规则）、PR 维度付款汇总、PR 号链接跳转
    - _需求: 33, 34, 35, 48_

  - [ ] 16.4* 编写端到端集成测试
    - 创建到审批通过并同步全流程、取消闭环、权限与数据范围隔离、PR 付款汇总
    - _需求: 33, 34, 35, 48_

- [ ] 17. 最终检查点 - 全模块完成
  - 确保所有测试通过，如有疑问请向用户确认。

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加速 MVP 交付
- 每个任务引用了对应的需求编号以确保可追溯性
- 检查点任务确保增量验证
- 后端与前端可并行开发，但联调需在双方基础功能完成后进行
- **外部依赖（Worklife BPM / 台账管理系统）本阶段全部空实现**：BPM 直接审批通过、台账同步直接成功（已同步/待付款）、付款回传不自动推进；端口与 `/api/external/**` 回调接口保留供后续真实对接
- **跨模块依赖**：模块 03（PR）、模块 05（合同）未实现，相关适配器先桩/占位，Phase 16 替换为真实集成
- **三类状态完全独立**：PO 审批状态、台账同步状态、付款状态各自独立存储/流转/展示，前后端均不得混用（Req 33.10、35.3）
- 付款状态与已付款金额仅由台账回调写入，台账回传为唯一可信来源（Req 34.6）
- PO 详情不展示「系统同步模块」与「PO 状态流程」说明卡（Req 35.6）；关联 PR 号可点击跳转（Req 35.7）
