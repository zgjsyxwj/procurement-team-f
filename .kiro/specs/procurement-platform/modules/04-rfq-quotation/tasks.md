# 实施计划：询报价管理模块

## 概述

基于 DDD 分层架构，按照数据库 → 领域层 → 基础设施层 → 应用层 → 接口层 → 前端的顺序逐步实现询报价管理模块。后端使用 Java 21 + Spring Boot 3.5，数据库 PostgreSQL；前端使用 Vue 3 + TypeScript + ant-design-vue。本模块依赖模块 01（账号体系、数据范围）、模块 02（合作中供应商及联系人）、模块 03（已审批 PR/PR 合集），并被模块 05（合同管理）依赖（合同创建时选择已完成核价的 RFQ）。供应商/联系人、PR 来源通过端口与上游模块集成；邮件、OSS 通过端口解耦，本阶段为占位实现。

## 任务列表

- [ ] 1. 数据库 Schema 与基础设施搭建
  - [ ] 1.1 创建数据库迁移脚本 `Vxx__init_rfq_tables.sql`
    - 创建 `rfq` 表（唯一索引 uk_rfq_code，索引 idx_rfq_pr / bundle / status / strategy / created_by）
    - 创建 `rfq_standard_item` 表（索引 idx_std_item_rfq）
    - 创建 `rfq_supplier` 表（唯一索引 uk_rfq_supplier，索引 idx_rfq_supplier_rfq / supplier）
    - 创建 `quote_round` 表（唯一索引 uk_round_no）
    - 创建 `quote` 表（部分唯一索引 uk_quote_valid WHERE is_valid，索引 idx_quote_rfq / round / supplier）
    - 创建 `quote_item` 表（索引 idx_quote_item_quote / std）
    - 创建 `quote_attachment` 表（索引 idx_quote_attachment_quote）
    - 创建 `price_review_item` 表（索引 idx_review_item_rfq / round / supplier）
    - 创建 `quote_item_mapping` 表（索引 idx_mapping_review_item / quote_item）
    - 创建 `price_adjustment` 表（索引 idx_adjustment_rfq / review_item）
    - 创建 `rfq_approval_log` 表（索引 idx_approval_log_rfq）
    - 创建 `rfq_operation_log` 表（索引 idx_operation_log_rfq）
    - 创建 `rfq_notification_log` 表（索引 idx_notification_rfq）
    - 编号沿用 PostgreSQL DDL 风格（BIGSERIAL/TIMESTAMP(3)/NUMERIC/BOOLEAN，独立 CREATE INDEX，无外键约束）；脚本号取当前可用编号，避免与模块 05 冲突
    - _需求: 17, 19, 22, 23, 24, 25, 26_

  - [ ] 1.2 添加项目依赖与配置
    - 配置 `RfqProperties`（默认税率 6%、默认币别 CNY、OSS 上传白名单、开标前金额加密密钥来源）
    - 启用定时任务 `@EnableScheduling`（报价截止/自动开标）
    - 复用模块 01/02 邮件配置发送各类通知邮件
    - _需求: 18.2, 19.4, 19.16, 22.1, 22.2, 22.6_

- [ ] 2. 领域层 - 模型与枚举
  - [ ] 2.1 实现枚举
    - `RfqStatus`：DRAFT, QUOTING, QUOTE_CLOSED, BID_OPENED, PRICE_REVIEWING, APPROVING, COMPLETED, REJECTED, CANCELLED
    - `ProcurementStrategy`：INQUIRY_COMPARISON, SINGLE_SOURCE, DIRECTED, RENEWAL
    - `QuoteStatus`：QUOTED, RETURNED
    - `QuoteSource`：SUPPLIER, PROXY
    - `BidResult`：WIN, LOSE
    - _需求: 17.4, 19.6, 20.2, 21.4, 22.7_

  - [ ] 2.2 实现 Rfq 聚合根、RfqStandardItem、RfqSupplier
    - `Rfq` 字段：rfqCode, projectName, projectDescription, strategy, status, prId/prBundleId, quoteStartTime/quoteEndTime/bidOpenTime, budgetAmount, showBudgetToSupplier, currentRound, reviewRoundId, winningSupplierId
    - 状态流转方法：`publish()`, `closeQuoting()`, `openBid()`, `startNewRound()`, `startPriceReview()`, `withdrawPriceReview()`, `submitApproval()`, `approve()`, `reject()`, `cancel()`，非法流转抛 `InvalidRfqStatusException`
    - `RfqStandardItem`：itemName, quantity, unitPrice, subtotal, deliveryDays, remark；小计计算；至少保留 1 行
    - `RfqSupplier`：supplierId, supplierName, notifyContactId, 联系人快照, bidResult
    - `canBeSelectedByContract()`：仅「已完成」可被合同选择
    - _需求: 17.3, 17.8, 17.9, 17.11, 17.15, 17.18, 18.1, 22, 23, 24, 25, 26.5_

  - [ ] 2.3 实现 QuoteRound、Quote、QuoteItem、QuoteAttachment
    - `QuoteRound`：roundNo, bargainNote, deadline, carryPrevious, priceOnly, allowNewItems, current
    - `Quote`：roundId, supplierId, status, valid, source, proxy 信息, taxRate, currency, paymentDays, paymentMethod, invoiceType, plan 信息, commitmentConfirmed, returnReason；方法 `submit()`, `return_(reason)`, `supersede()`, `calcPlanTotal()`（折扣项负数计入）
    - `QuoteItem`：stdItemId, freeItem, itemName, quantity, unitPrice, subtotal, deliveryDays, 扩展字段（itemType/specification/uom/isSubstitute/prItemRef）
    - `QuoteAttachment`：fileUrl, fileName, fileSize, isProxyEvidence
    - 小计（数量×单价）与含税合计（各行小计之和×(1+税率)）计算
    - _需求: 19.4, 19.5, 19.13, 19.14, 19.18, 21.1, 21.4, 23.5_

  - [ ] 2.4 实现 PriceReviewItem、QuoteItemMapping、PriceAdjustment
    - `PriceReviewItem`：caliberName, supplierId, aggregatedAmount, lowest, winnerMarked, finalPrice, finalSubtotal, mappedItems
    - `QuoteItemMapping`：reviewItemId, quoteItemId（支持 1→N / N→1）
    - `PriceAdjustment`：originalUnitPrice/Subtotal, finalPrice/Subtotal, reason, operator, createdAt（不覆盖原始）
    - _需求: 24.2-24.9, 24.13, 24.14_

- [ ] 3. 领域层 - 领域服务与接口
  - [ ] 3.1 实现 RfqLifecycleService（状态机）
    - 集中校验并执行 RFQ 状态合法流转（含代询价快路径 QUOTING→QUOTE_CLOSED→BID_OPENED、多轮报价回退、撤回核价）
    - 编辑锁定规则（草稿全可编辑 / 报价中无报价可编辑 / 有报价锁核心字段）
    - _需求: 18.1, 18.5-18.8, 21.5, 22, 23.1, 23.7, 23.8, 26.5_

  - [ ] 3.2* 编写 RfqLifecycleService 单元测试
    - 测试各合法流转与非法流转拒绝、代询价快路径、撤回核价回退
    - _需求: 22, 23.7_

  - [ ] 3.3 实现 RfqCreationService（带出/校验/快照）
    - 校验开标时间晚于报价结束时间
    - 采购策略与供应商数量约束（询比价多家+通知人；单一来源/定向/续约仅 1 家+不通知），违规抛 `SupplierCountViolationException`
    - 带出 PR 预算金额/币种快照、参与供应商基础信息快照
    - 标准行删除前校验是否已被报价引用，违规抛 `StandardItemInUseException`
    - _需求: 17.3-17.9, 17.14-17.16, 17.18_

  - [ ] 3.4* 编写 RfqCreationService 单元测试
    - 测试时间校验、策略-供应商数量约束、标准行引用拦截
    - _需求: 17.6, 17.9, 17.18_

  - [ ] 3.5 实现 PriceAggregationService（报价归集/比价矩阵）
    - 原始报价项映射到统一核价口径（1→N / N→1）
    - 生成比价矩阵（行=核价口径，列=供应商方案，单元格=归集金额/来源/附件状态/核价后价/改价）
    - 自动标记每个口径项最低报价供应商
    - 提交审批前校验推荐供应商方案已完成必要归集，违规抛 `AggregationIncompleteException`
    - _需求: 24.2-24.5, 24.10, 24.14_

  - [ ] 3.6* 编写 PriceAggregationService 单元测试
    - 测试 1→N / N→1 映射、最低价标记、归集完整性校验
    - _需求: 24.3, 24.5, 24.10_

  - [ ] 3.7 实现 RfqCodeGenerator
    - 询价单编号生成：RFQ-YYYYMM-5 位自增
    - _需求: 17.11_

  - [ ] 3.8 定义领域仓储接口
    - `RfqRepository`、`QuoteRepository`（含轮次、报价、归集、改价子对象）
    - _需求: 17, 19, 24, 26_

  - [ ] 3.9 定义领域端口接口
    - `NotificationPort`：发布/退回/新一轮/审批待办/中标落标通知
    - `FileStoragePort`：OSS 报价/证据附件、模板上传下载
    - `ActiveSupplierPort`：合作中供应商及联系人（模块 02）
    - `PrSourcePort`：可选 PR/PR 合集 + 预算带出（模块 03）
    - `QuoteAmountCryptoPort`：开标前金额加解密
    - _需求: 17.1, 17.5, 18.2, 19.16, 22.6, 22.7_

- [ ] 4. 检查点 - 领域层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 5. 基础设施层 - 持久化
  - [ ] 5.1 实现 JPA 实体映射
    - 创建 `RfqEntity`、`RfqStandardItemEntity`、`RfqSupplierEntity`、`QuoteRoundEntity`、`QuoteEntity`、`QuoteItemEntity`、`QuoteAttachmentEntity`、`PriceReviewItemEntity`、`QuoteItemMappingEntity`、`PriceAdjustmentEntity`、`RfqApprovalLogEntity`、`RfqOperationLogEntity`、`RfqNotificationLogEntity`
    - `rfq` 含乐观锁 `@Version`；金额加密列以 `*_enc` 字符串存储
    - _需求: 17, 19, 22.6, 24_

  - [ ] 5.2 实现领域对象与实体的 Mapper
    - `RfqMapper`、`QuoteMapper`（含标准行/供应商/轮次/明细/归集/改价子对象转换）
    - _需求: 17, 19, 24_

  - [ ] 5.3 实现 JPA Repository
    - 实现各仓储接口；RFQ 列表支持项目名/PR号模糊、状态/策略筛选与分页、按创建人数据范围过滤
    - 报价按轮次/供应商/有效版本查询；核价取最新有效轮次报价
    - 截止/开标到期 RFQ 扫描查询
    - _需求: 22.1, 22.2, 23.10, 26.1-26.4_

- [ ] 6. 基础设施层 - 外部适配器、加密与定时任务
  - [ ] 6.1 实现 OssFileStorageAdapter（FileStoragePort）
    - 报价附件、代询价证据附件、报价模板上传至 OSS、生成下载地址
    - 文件格式（PDF/Word/Excel/JPG/PNG/ZIP/RAR）与大小（≤100MB）白名单校验，拒绝高风险格式
    - _需求: 19.16, 21.2, 21.7_

  - [ ] 6.2 实现 ActiveSupplierAdapter 与 PrSourceAdapter
    - `ActiveSupplierAdapter`：调模块 02 `/api/internal/suppliers/active` 取合作中供应商及联系人
    - `PrSourceAdapter`：调模块 03 取可选已审批 PR/PR 合集（按数据范围 + 筛选）并带出预算
    - _需求: 17.1, 17.2, 17.5, 17.12, 17.14, 17.15_

  - [ ] 6.3 实现 EmailServiceAdapter（NotificationPort）
    - 发布通知、退回通知、新一轮通知、审批待办、中标/落标结果邮件
    - _需求: 18.2, 20.2, 22.7, 23.3, 25.2_

  - [ ] 6.4 实现 QuoteAmountCryptoService（QuoteAmountCryptoPort）
    - 报价提交时对单价/小计/方案总价/含税合计 AES 加密入库；开标后解密对采购端展示
    - _需求: 22.6_

  - [ ] 6.5 实现报价截止与开标定时任务
    - `RfqTimerScheduler`（@Scheduled 周期触发）：到达报价结束时间 QUOTING→QUOTE_CLOSED；到达开标时间 QUOTE_CLOSED→BID_OPENED，记录操作流水
    - _需求: 22.1, 22.2_

  - [ ] 6.6* 编写定时任务与加密测试
    - 测试到期流转、加密往返、开标前不可解密
    - _需求: 22.1, 22.2, 22.6_

- [ ] 7. 检查点 - 基础设施层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 8. 应用层 - 命令与查询
  - [ ] 8.1 实现询价单创建与编辑 Command 与 Handler
    - `CreateRfqCommand`、`UpdateRfqDraftCommand`
    - `RfqCommandHandler`：校验时间/策略-供应商约束 → 生成编号 → 保存 RFQ(DRAFT)+标准行+参与供应商；编辑按状态与是否已有报价裁决可编辑范围
    - _需求: 17, 18.5-18.8_

  - [ ] 8.2 实现发布、关闭报价、开标、取消 Command 与 Handler
    - `PublishRfqCommand`（DRAFT→QUOTING，询比价发通知建报价入口；其余三策略不发通知）、`CloseQuotingCommand`、`OpenBidCommand`、`CancelRfqCommand`（填原因+通知供应商）
    - 普通采购员仅可发布本人 RFQ；采购经理/Admin 不受限
    - _需求: 18.1-18.4, 22.3, 22.4, 26.5, 26.6_

  - [ ] 8.3 实现供应商报价与退回 Command 与 Handler
    - `SubmitQuoteCommand`（带出标准行+自由行、税率/币别/账期/付款方式/发票类型/附件、报价承诺确认、金额加密、锁定本轮版本）、`ReturnQuoteCommand`（填原因、旧版本置失效、通知供应商）
    - 校验在报价时间段内且 RFQ=QUOTING；退回仅在 quote=QUOTED 且未开标
    - _需求: 19.4-19.8, 19.12-19.17, 20.1-20.5_

  - [ ] 8.4 实现代询价 Command 与 Handler
    - `ProxyQuoteCommand`：字段同供应商 + 证据附件必填，标注 source=PROXY + 操作采购员；提交后置 QUOTED
    - 仅在 RFQ=QUOTING 时可执行；关闭报价时一次性流转至 BID_OPENED 并记录流水
    - _需求: 21.1-21.8_

  - [ ] 8.5 实现多轮报价 Command 与 Handler
    - `StartNewRoundCommand`：仅 BID_OPENED 可开启，轮次号自增、状态重置 QUOTING、通知供应商；配置议价说明/供应商/截止/带出上一轮/仅调价/允许新增项
    - 历史轮次报价锁定保留，默认最新有效轮次为核价轮次
    - _需求: 23.1-23.6, 23.9, 23.10_

  - [ ] 8.6 实现核价、报价归集与改价 Command 与 Handler
    - `StartPriceReviewCommand`（BID_OPENED→PRICE_REVIEWING）、`WithdrawPriceReviewCommand`（→BID_OPENED，记原因/人/时间）、`AggregateQuoteItemsCommand`、`SaveBidResultCommand`（整单中标+中标标记）、`AddPriceAdjustmentCommand`（选明细+最终成交价+原因，不覆盖原始）
    - _需求: 23.7, 23.8, 23.11, 24.1-24.14_

  - [ ] 8.7 实现核价审批 Command 与 Handler
    - `SubmitApprovalCommand`（校验已完成必要归集、确认快照，PRICE_REVIEWING→APPROVING，通知采购经理）、`ApproveCommand`（→COMPLETED，发中标/落标邮件）、`RejectCommand`（填原因→REJECTED，通知采购员）
    - 记录审批时间/审批人/结果
    - _需求: 24.10, 25.1-25.7, 22.7_

  - [ ] 8.8 实现查询与数据范围服务
    - `RfqListQuery`、`RfqDetailQuery`、`SelectableSuppliersQuery`、`SelectablePrQuery`、`QuoteComparisonQuery`、`PriceMatrixQuery`、`SupplierRfqListQuery`、`RfqResultQuery`
    - `RfqQueryHandler`：列表分页/搜索/筛选、详情、开标后报价对比、比价矩阵、供应商端 RFQ 列表、供模块 05 的已核价结果
    - `RfqAccessService`：按角色/创建人过滤（ADMIN 全量 / BUYER 本人 / SUPPLIER 本企业），开标前金额一律不下发
    - _需求: 17.1, 18.4, 19.1-19.3, 22.5, 22.6, 26.1-26.4, 依赖关系_

- [ ] 9. 接口层 - REST Controller
  - 角色门禁：模块 01 `SecurityConfig` 加路径规则——`/api/rfqs/**` 限 BUYER/ADMIN（审批 approve/reject 限 ADMIN）；`/api/supplier/rfqs/**`、`/api/supplier/quote-template` 限 SUPPLIER；`/api/internal/**` 内部调用放行。
  - [ ] 9.1 实现 DTO
    - 创建/列表/详情、可选 PR/供应商、报价提交/退回/代询价、新一轮、归集/比价矩阵、中标结果/改价、提交审批/审批决定、报价对比、模块 05 结果等请求/响应 DTO
    - _需求: 17, 19, 20, 21, 23, 24, 25, 26_

  - [ ] 9.2 实现 RfqController（采购端 - 询价单管理）
    - `GET selectable-prs / selectable-suppliers`、`POST/GET/PUT /api/rfqs(/{id})`、`publish`、`close-quoting`、`open-bid`、`comparison`、`cancel`、`POST /{id}/rounds`
    - _需求: 17, 18, 22.3-22.5, 23, 26_

  - [ ] 9.3 实现 RfqPriceReviewController（采购端 - 报价处理与核价）
    - 退回报价、代询价、发起核价、撤回核价、报价归集、比价矩阵、确认中标、新增改价、提交审批
    - _需求: 20, 21, 23.7, 24, 25.1, 25.7_

  - [ ] 9.4 实现采购经理审批接口（approve / reject，限 ADMIN）
    - `POST /api/rfqs/{id}/approve`、`POST /api/rfqs/{id}/reject`
    - _需求: 25.3, 25.4, 22.7_

  - [ ] 9.5 实现 SupplierQuoteController（供应商端）
    - `GET /api/supplier/rfqs(/{id})`、`GET/POST /api/supplier/rfqs/{id}/quote`、`GET /api/supplier/quote-template`
    - _需求: 19.1-19.6, 19.10, 19.16, 19.17, 20.5_

  - [ ] 9.6 实现 RfqInternalController（模块集成）
    - `GET /api/internal/rfqs/completed`：PR/PR合集下已完成核价审批的 RFQ 结果（供模块 05）
    - _需求: 依赖关系_

  - [ ] 9.7* 编写 Controller 集成测试
    - 测试创建/发布、报价/退回、代询价、开标、核价/归集/改价、审批、权限与数据范围隔离、开标前金额屏蔽
    - _需求: 17, 19, 21, 22, 24, 25_

- [ ] 10. 检查点 - 后端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 11. 前端 - 基础设施与状态管理
  - [ ] 11.1 实现前端类型定义
    - `rfq.dto.ts`、`quote.dto.ts`、`price-review.dto.ts`、`rfq-info.vo.ts`（枚举 + 中文标签/颜色映射）、command 类型
    - _需求: 17, 19, 24, 26_

  - [ ] 11.2 实现 service 层（API 调用）
    - `rfq.service.ts`、`quote.service.ts`、`price-review.service.ts`、`supplier-quote.service.ts`，复用 `shared/http` axios 与 CSRF 适配器
    - _需求: 17, 19, 20, 21, 22, 23, 24, 25, 26_

  - [ ] 11.3 实现 oss-upload.adapter.ts
    - 报价/证据附件上传（格式/大小前端预校验）
    - _需求: 19.16, 21.2_

  - [ ] 11.4 实现 rfq.store.ts（Pinia）
    - 当前 RFQ、列表筛选状态、供应商端待报价计数
    - _需求: 26.1, 26.2_

- [ ] 12. 前端 - 领域与用例层
  - [ ] 12.1 实现前端领域模型与规则
    - `rfq.entity.ts`、`quote.entity.ts`、`rfq-status.vo.ts`、`procurement-strategy.vo.ts`
    - 规则：`quote-time-validation.rule.ts`（开标>报价结束、报价时间段内）、`quote-amount.rule.ts`（小计/含税合计/折扣负数）
    - _需求: 17.9, 19.5, 19.14_

  - [ ] 12.2 实现前端用例层
    - create-rfq、manage-rfqs、close-and-open-bid、return-quote、proxy-quote、new-round、price-review、approval、supplier-quote
    - _需求: 17, 18, 20, 21, 22, 23, 24, 25, 26_

- [ ] 13. 前端 - 采购/管理端页面与组件
  - [ ] 13.1 实现 RfqCreateView（分步创建）
    - 步骤：选 PR/PR合集（PrSourceSelector，筛选）→ 基本信息（RfqBasicForm，项目名/概述/时间/策略/预算展示开关）→ 标准报价行（StandardItemTable）→ 选供应商+指定通知人（SupplierSelector，筛选）
    - 客户端校验（开标>报价结束、策略-供应商数量、标准行至少 1 行）
    - _需求: 17_

  - [ ] 13.2 实现 RfqListView
    - 列表字段（编号/项目名/PR号/策略/状态/可报价时间段/开标时间/参与供应商数/创建时间）
    - 项目名/PR号模糊搜索、状态/策略筛选、分页、取消（CancelRfqDialog）
    - _需求: 26_

  - [ ] 13.3 实现 RfqDetailView
    - 信息 / 参与供应商 / 报价对比 / 操作记录 Tab
    - 发布、关闭报价、手动开标、代询价（ProxyQuoteDialog）、退回报价（ReturnQuoteDialog）、开启新一轮（NewRoundDialog）
    - 开标后报价汇总对比（QuoteComparisonTable，开标前屏蔽金额）
    - _需求: 18, 20, 21, 22.5, 23_

  - [ ] 13.4 实现 PriceReviewView（核价）
    - 报价归集（AggregationDialog）、比价矩阵（PriceMatrix，含归集金额/来源/附件/最低价/改价）、确认中标、新增改价（PriceAdjustmentDialog）、撤回核价、提交采购经理审批（确认弹窗快照）
    - _需求: 23.7, 24, 25.1, 25.7_

- [ ] 14. 前端 - 供应商端页面
  - [ ] 14.1 实现 SupplierRfqListView
    - 发给本企业的询价单列表，区分状态
    - _需求: 19.1, 19.2_

  - [ ] 14.2 实现 SupplierQuoteView（报价填写）
    - 详情展示 + 报价表单（带出标准行+自由行 QuoteItemTable、税率/币别/账期/付款方式/发票类型、附件上传、报价承诺确认 QuoteCommitmentDialog）
    - 退回原因展示与重新提交、报价结束后禁止提交
    - _需求: 19.3-19.8, 19.12-19.17, 20.5_

- [ ] 15. 前端 - 路由与权限守卫
  - [ ] 15.1 实现 rfq.routes.ts
    - 采购端（BUYER/ADMIN）、供应商端（SUPPLIER）路由与角色守卫
    - _需求: 17.1, 19.1, 26.4_

  - [ ] 15.2 配置角色菜单
    - 在 `frontend/src/config/menu.ts` 按角色加入询价单管理（采购端）/ 我的询价单（供应商端）入口
    - _需求: 26.1_

- [ ] 16. 检查点 - 前端完成
  - 确保 `npm run build` 通过，如有疑问请向用户确认。

- [ ] 17. 集成与端到端验证
  - [ ] 17.1 实现共享常量与异常处理
    - `RfqConstants`（编号前缀、默认税率/币别、文件白名单、报价承诺文案）
    - 自定义异常：`RfqNotFoundException`、`InvalidRfqStatusException`、`QuoteWindowClosedException`、`StandardItemInUseException`、`SupplierCountViolationException`、`AggregationIncompleteException`，接入 `GlobalExceptionHandler.resolveHttpStatus`（登记对应 HTTP 状态）
    - _需求: 17.6, 17.18, 19.7, 24.10_

  - [ ] 17.2 跨模块集成联调
    - 模块 02 合作中供应商/联系人、模块 03 已审批 PR/预算带出、模块 05 已核价 RFQ 结果带出
    - _需求: 17.1, 17.5, 依赖关系_

  - [ ] 17.3 前后端联调验证
    - 询比价全流程：创建→发布→供应商报价→（退回/重提）→截止→开标→核价归集→改价→提交审批→通过→中标/落标邮件
    - 代询价流程：创建（单一来源/定向/续约）→代录报价→关闭报价→开标→核价→审批
    - 多轮报价、取消流程
    - _需求: 18, 19, 21, 22, 23, 24, 25, 26_

  - [ ] 17.4* 编写端到端集成测试
    - 询比价闭环、代询价闭环、多轮报价、开标前金额保密、权限与数据范围隔离
    - _需求: 19, 21, 22.6, 23, 26.4_

- [ ] 18. 最终检查点 - 全模块完成
  - 确保所有测试通过，如有疑问请向用户确认。

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加速 MVP 交付
- 每个任务引用了对应的需求编号以确保可追溯性
- 检查点任务确保增量验证
- 后端与前端可并行开发，但联调需在双方基础功能完成后进行
- 本模块依赖模块 01/02/03，被模块 05 依赖；上游来源（PR、供应商）与下游消费（合同）均通过端口/内部接口解耦
- 校验逻辑（开标>报价结束、策略-供应商数量、报价时间段、小计/含税合计、文件白名单）前后端均需实现，后端为权威校验
- 开标前报价金额保密为本模块安全要点（加密存储 + API 不下发 + 前端屏蔽，Req 22.6）
- 当前版本采用整单中标供应商模式；分项中标 / 多供应商拆分、复核维度矩阵、多报价方案为后续备用能力（Req 24.11、24.12、19.13）
- 邮件、OSS、定时任务的真实对接可按上游模块的占位决策延后，端口边界先行
