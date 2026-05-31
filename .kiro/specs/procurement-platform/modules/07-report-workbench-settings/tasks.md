# 实施计划：报表 / 工作台 / 数据设置模块

## 概述

基于 DDD 分层架构，按数据库 → 领域层 → 基础设施层 → 应用层 → 接口层 → 前端的顺序实现模块 07。实现横跨 `report`（报表/工作台/审批中心/邮件）与 `setting`（动态表单引擎）两个后端包，以及 `shared`（统一编号）。后端 Java Spring Boot + PostgreSQL，前端 Vue 3 + TypeScript + ant-design-vue。

报表与工作台为只读聚合层，不新建业务表、不复制业务数据；审批中心、邮件、表单引擎、编号为新建能力。

## 任务列表

- [ ] 1. 数据库 Schema 与基础设施
  - [ ] 1.1 创建数据库迁移脚本（V4 之后下一可用编号）
    - 创建 `number_sequence` 表（含唯一索引 uk_seq_prefix_period）
    - 创建 `email_log` 表（含索引 idx_email_status/type/buyer/supplier/sent_at）
    - 创建 `approval_task` 表（含唯一索引 uk_approval_no、uk_approval_biz，索引 idx_approval_status/approver/initiator）
    - 创建 `form_template` 表
    - 创建 `field_definition` 表（含唯一索引 uk_field_template_code）
    - 创建 `form_field_config` 表（含唯一索引 uk_form_field）
    - 创建 `form_submission_snapshot` 表
    - 插入默认表单模板与默认字段样例（PR 接收、供应商企业信息、供应商创建、证件上传、联系人）
    - _需求: 13.5, 13.22, 41.4, 42.2, 39.6_

- [ ] 2. shared - 统一编号
  - [ ] 2.1 实现 `NumberPrefix` 枚举与 `NumberPort` 接口
    - 定义前缀：VD/PR/RFQ/QT/PO/CA/AP/CE/CO
    - 定义 `NumberPort.next(prefix)` / `next(prefix, supplierSuffix)`
    - _需求: 42.1_

  - [ ] 2.2 实现 `SequenceNumberGenerator`
    - 基于 `number_sequence` 行级锁/乐观锁自增，保证全局唯一
    - VD：4 位无周期自增（VD0001）；其余：PREFIX-YYYYMM-5位；QT 追加供应商后缀
    - 周期（YYYYMM）切换时新建序列行
    - 单据创建时自动生成、不可人工编辑
    - _需求: 42.1, 42.2, 42.3_

  - [ ]* 2.3 编写 SequenceNumberGenerator 单元测试
    - 测试各前缀格式正确
    - 测试同前缀同周期并发自增唯一（多线程）
    - 测试跨月周期切换重新计数
    - _需求: 42.1, 42.2_

- [ ] 3. report 领域层 - 模型与枚举
  - [ ] 3.1 实现报表相关枚举与值对象
    - `AmountCaliber`、`ReportDimension`、`RiskType`
    - `ReportScope` 值对象（全量 / 限定 buyerId / 限定本人）
    - `MetricCard` 值对象
    - _需求: 36.3, 36.4, 36.7, 36.9_

  - [ ] 3.2 实现 `ReportScopeResolver`
    - 由 `SecurityUtils` 当前用户与角色解析数据范围：采购员限本人、Admin/经理全量、业务人员限本人
    - _需求: 36.1, 38.3, 58.4, 44.5_

  - [ ] 3.3 实现审批待办领域模型
    - `ApprovalTask` 聚合（approve/reject/cancel 状态流转）
    - `ApprovalTaskType`、`ApprovalTaskStatus` 枚举
    - _需求: 39.1, 39.5, 39.6_

  - [ ] 3.4 实现邮件日志领域模型
    - `EmailLog` 聚合（markFailure、canResend）
    - `EmailType`、`EmailStatus` 枚举
    - _需求: 41.4, 53.3_

  - [ ] 3.5 定义仓储与端口接口
    - `ApprovalTaskRepository`、`EmailLogRepository`
    - `ApprovalTaskPort`（register/complete/cancel，供业务模块调用）
    - `NotificationPort`（统一发信入口）
    - `ReportDataQueryPort`（只读聚合查询）
    - _需求: 39.3, 41.1, 41.4, 36.4_

- [ ] 4. setting 领域层 - 动态表单引擎
  - [ ] 4.1 实现表单/字段领域模型与枚举
    - `FormTemplate` 聚合根、`FieldDefinition`、`FormFieldConfig`、`ValidationRule`
    - `BusinessObject`、`ControlType`、`DataType`、`FieldStatus` 枚举
    - _需求: 13.1, 13.3, 13.4, 13.5_

  - [ ] 4.2 实现 `FieldCodeUniquenessService`
    - 校验字段编码在所属表单字段库内唯一
    - _需求: 13.2_

  - [ ] 4.3 实现 `FormValidationService`
    - 按当前模板配置校验提交字段值：必填、格式、文件类型/大小、敏感脱敏、显示条件、校验覆盖
    - _需求: 13.6, 13.9, 13.10, 45.4_

  - [ ] 4.4 定义仓储与端口接口
    - `FormTemplateRepository`、`FieldDefinitionRepository`、`FormFieldConfigRepository`
    - `FormConfigQueryPort`（供业务模块读取当前生效配置）
    - _需求: 13.10, 13.21_

  - [ ]* 4.5 编写表单校验/编码唯一单元测试
    - 测试库内编码重复被拒、跨库可重名
    - 测试必填/格式/文件白名单/敏感脱敏校验
    - _需求: 13.2, 13.6_

- [ ] 5. 检查点 - 领域层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 6. report 基础设施层
  - [ ] 6.1 实现只读聚合仓储 `ReportQueryRepository`（实现 `ReportDataQueryPort`）
    - 指标卡聚合（本月采购金额/已付款/进行中/平均核价周期/合同完成率/付款完成率/异常数）
    - 多维金额聚合（按 dept/cost_center/supplier/category/currency/buyer，按 currency 分组原币）
    - 各阶段周期聚合（PR→RFQ、RFQ发布→截止、开标→核价通过、合同创建→完成、PO→付款）
    - 供应商排行/参与/中标/中标率
    - 风险清单聚合（5 类风险类型）
    - 流程漏斗各阶段数量
    - 按采购员统计
    - 图表数据（趋势/排行/状态分布）
    - 数据范围与 caliber 注入 WHERE
    - _需求: 36.3, 36.4, 36.5, 36.6, 36.7, 36.9, 36.10, 36.11, 36.12, 36.14_

  - [ ] 6.2 实现审批/邮件 JPA 持久化
    - `ApprovalTaskEntity` + Mapper + `JpaApprovalTaskRepository`（按 tab/type/approver/initiator 分页）
    - `EmailLogEntity` + Mapper + `JpaEmailLogRepository`（按类型/状态/时间/范围分页）
    - _需求: 39.1, 39.6, 53.1, 53.2, 53.4_

  - [ ] 6.3 实现 `EmailServiceAdapter`（占位）+ `EmailNotificationService`
    - `EmailNotificationService` 实现 `NotificationPort`：组装邮件 → 调适配器 → 落 `email_log`（成功/失败+原因）→ 支持重发
    - `EmailServiceAdapter` 占位：不实际投递，按约定记录状态
    - _需求: 41.1, 41.2, 41.3, 41.4, 41.5, 53.5_

- [ ] 7. setting 基础设施层
  - [ ] 7.1 实现表单引擎 JPA 持久化
    - `FormTemplateEntity`、`FieldDefinitionEntity`、`FormFieldConfigEntity`、`FormSubmissionSnapshotEntity` + Mapper
    - `JpaFormTemplateRepository`、`JpaFieldDefinitionRepository`、`JpaFormFieldConfigRepository`、`JpaFormSubmissionSnapshotRepository`
    - _需求: 13.1, 13.4, 13.7_

  - [ ] 7.2 实现 `FormConfigService`（实现 `FormConfigQueryPort`）
    - 按 businessObject 读取当前生效模板 + 字段 + 配置，组装渲染配置
    - 保存提交快照
    - _需求: 13.6, 13.7, 13.10, 13.21_

- [ ] 8. 检查点 - 基础设施层完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 9. report 应用层
  - [ ] 9.1 实现报表 Query 与 `ReportQueryHandler`
    - `ReportOverviewQuery`、`AmountStatQuery`、`EfficiencyStatQuery`、`SupplierRankingQuery`、`RiskListQuery`、`FunnelQuery`、`BuyerStatQuery`、`ChartQuery`
    - Handler 解析 scope + caliber → 调聚合端口 → 组装响应（回显 caliber）
    - _需求: 36.2, 36.3, 36.4, 36.5, 36.6, 36.7, 36.9, 36.11, 36.12, 36.13, 36.14_

  - [ ] 9.2 实现工作台 `WorkbenchQueryHandler`
    - `WorkbenchQuery`（按角色）：采购员（合作中供应商数/进行中统计/快捷操作，排除待建合同/草稿/已完成）
    - 采购经理（报表视角金额/进行中/效率/风险，不含审批列表）
    - 业务人员首页（本人 PR 分布/审批中 PR/可建 PO 合同/PO 审批与付款进度）
    - _需求: 37.1, 37.4, 37.5, 38.1, 38.2, 38.3, 58.1, 58.3, 58.4_

  - [ ] 9.3 实现审批中心 `ApprovalQueryHandler`
    - `ApprovalListQuery`（tab=PENDING/INITIATED/COMPLETED，默认 PENDING；type 过滤；分页）
    - 待办详情（含 bizType/bizId 深链元数据）
    - _需求: 39.1, 39.2, 39.3, 39.6_

  - [ ] 9.4 实现邮件日志 `EmailLogHandler` 与重发
    - `EmailLogQuery`（按当前用户范围 + 类型/状态/时间筛选）
    - `ResendEmailCommand`（失败邮件手动重发）
    - _需求: 53.1, 53.2, 53.3, 53.4, 53.5, 41.5_

  - [ ]* 9.5 编写报表/工作台/审批 Handler 单元测试
    - 测试采购员数据范围只见本人、经理见全量
    - 测试 caliber 切换返回不同口径并回显
    - 测试审批三 Tab 过滤
    - _需求: 36.1, 36.9, 38.3, 39.1_

- [ ] 10. setting 应用层
  - [ ] 10.1 实现字段设置 Command/Query Handler
    - `CreateFieldCommand`（先选字段库，库内编码唯一）、`UpdateFieldCommand`、`ToggleFieldStatusCommand`
    - `FieldLibraryListQuery`（仅字段库列表）、`FieldLibraryDetailQuery`、`FieldDetailQuery`
    - _需求: 13.1, 13.2, 13.11, 13.12, 13.13, 13.14, 40.3_

  - [ ] 10.2 实现表单设置 Command/Query Handler
    - `CreateFormTemplateCommand`（业务对象/使用端/数据保存方式/快照策略，生成独立字段库）
    - `ConfigureFormFieldsCommand`（增/编/启停 + 分组/排序/必填/只读/默认值/显示条件/校验覆盖/移除）
    - `FormListQuery`（仅表单列表）、`FormDetailQuery`、`FormPreviewQuery`
    - 保存即生效，无草稿/发布状态
    - _需求: 13.3, 13.4, 13.15, 13.16, 13.17, 13.18, 13.19, 13.20, 40.4, 40.5_

- [ ] 11. 接口层 - REST Controller
  - [ ] 11.1 实现 report 相关 DTO
    - 报表响应：Overview/AmountStat/Efficiency/SupplierRanking/RiskItem/Funnel/BuyerStat/Chart
    - Workbench（buyer/manager/business）、ApprovalTask、EmailLog 响应
    - _需求: 36.3, 37.1, 38.1, 39.1, 53.3_

  - [ ] 11.2 实现 `ReportController`
    - `GET /api/reports/overview|amount|efficiency|supplier-ranking|risks|funnel|buyer-stats|charts`
    - 通用筛选 query：startDate/endDate/deptId/buyerId/supplierId/category/currency/caliber
    - _需求: 36.2, 36.3, 36.4, 36.5, 36.6, 36.7, 36.11, 36.12, 36.13, 36.14_

  - [ ] 11.3 实现 `WorkbenchController`
    - `GET /api/workbench/buyer|manager|business`
    - _需求: 37, 38, 58_

  - [ ] 11.4 实现 `ApprovalCenterController`
    - `GET /api/approvals`（tab/type/分页）、`GET /api/approvals/{id}`（深链详情）
    - 审批通过/驳回不在此实现，深链至所属模块接口
    - _需求: 39.1, 39.2, 39.3, 39.4_

  - [ ] 11.5 实现 `EmailLogController`
    - `GET /api/email-logs`、`POST /api/email-logs/{id}/resend`
    - _需求: 53.1, 53.2, 53.4, 53.5, 41.5_

  - [ ] 11.6 实现 setting 相关 DTO 与 Controller
    - `FieldSettingController`：libraries 列表/详情、字段 CRUD/启停
    - `FormSettingController`：表单列表/详情/创建、配置字段、预览
    - `FormRuntimeController`：`GET /api/forms/{businessObject}/active`
    - _需求: 13.11, 13.13, 13.15, 13.18, 13.19, 13.20, 13.21, 40.3, 40.4_

  - [ ]* 11.7 编写 Controller 集成测试
    - 报表权限/数据范围、下钻仅返回 bizType/bizId
    - 字段库内编码唯一、表单保存即生效
    - 邮件日志范围隔离（供应商仅见本企业）
    - _需求: 36.8, 13.2, 44.5, 53.2_

- [ ] 12. 检查点 - 后端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 13. 集成 - 横切对接既有模块（触碰既有代码，需逐项确认）
  - [ ] 13.1 统一编号委托改造
    - 将既有 `RfqCodeGenerator`、`ContractCodeGenerator` 等改为委托 `SequenceNumberGenerator`
    - 保证既有编号格式不变、全局唯一
    - _需求: 42.1, 42.2_

  - [ ] 13.2 审批待办注册接入
    - 在 RFQ 核价审批、合同审批回调、PO 审批、供应商信息变更审核、供应商证件审核、采购员账号变更处调用 `ApprovalTaskPort.register/complete`
    - 驳回时填原因并经 `NotificationPort` 通知发起人
    - _需求: 39.3, 39.5, 39.6_

  - [ ] 13.3 邮件通知接入
    - 各模块发信改走统一 `NotificationPort`，落 `email_log` 并标注关联单据与可见范围
    - _需求: 41.1, 41.2, 41.3, 41.4_

  - [ ] 13.4 动态表单接入业务页面
    - PR 接收、供应商信息、证件上传、联系人提交时调用 `FormConfigQueryPort` 渲染依据 + `FormValidationService` 服务端校验 + 保存提交快照
    - _需求: 13.7, 13.9, 13.10, 13.21_

- [ ] 14. 前端 - report 模块
  - [ ] 14.1 实现类型与 service 层
    - `report.dto.ts`/`workbench.dto.ts`/`approval.dto.ts`/`email-log.dto.ts`
    - `report.service.ts`/`workbench.service.ts`/`approval.service.ts`/`email-log.service.ts`（复用 api-client）
    - _需求: 36.2, 37.1, 39.1, 53.3_

  - [ ] 14.2 实现报表管理页 `ReportDashboardView.vue`
    - 通用筛选条（时间/部门/采购员/供应商/品类/币种）+ 口径选择，页面明确当前口径
    - 指标卡、采购支出趋势、部门/供应商排行、RFQ/合同/PO 付款状态分布、流程漏斗、风险清单
    - `useDrilldown` 下钻跳转所属模块详情路由（不绕过权限）
    - 多币种按原币分别展示
    - _需求: 36.2, 36.3, 36.8, 36.9, 36.10, 36.11, 36.13, 36.14_

  - [ ] 14.3 实现三类工作台/首页
    - `BuyerWorkbenchView.vue`（快捷操作：创建供应商/询价单/合同申请；不含待建合同/草稿/已完成）
    - `ManagerWorkbenchView.vue`（报表视角；不含审批列表；点击跳报表/详情不越权）
    - `BusinessHomeView.vue`（创建采购申请/我的 PR/我的 PO/我的合同/待处理事项；不含 RFQ/供应商库/经理报表）
    - _需求: 37.1, 37.2, 37.3, 37.4, 37.5, 38.1, 38.2, 38.4, 58.1, 58.2, 58.3, 58.4, 58.5, 58.6_

  - [ ] 14.4 实现审批中心 `ApprovalCenterView.vue`
    - 三 Tab（需要我审批/我发起的/审批完成），默认「需要我审批」
    - 待办列表 + 按类型筛选 + 深链至所属模块审批/详情页
    - _需求: 39.1, 39.2, 39.3, 39.6_

  - [ ] 14.5 实现邮件日志 `EmailLogView.vue`
    - 列表（类型/收件人/时间/状态/关联单据）+ 筛选 + 失败重发（二次确认）
    - 采购员端与供应商端各自范围
    - _需求: 53.1, 53.2, 53.3, 53.4, 53.5_

- [ ] 15. 前端 - setting 模块（动态表单引擎）
  - [ ] 15.1 实现类型与 service 层
    - `field.dto.ts`/`form.dto.ts`；`field-setting.service.ts`/`form-setting.service.ts`/`form-runtime.service.ts`
    - _需求: 13.1, 13.4_

  - [ ] 15.2 实现数据设置菜单与字段设置子页面流程
    - `DataSettingLayout.vue`（采购员账号 → 复用模块 01 用户管理；字段设置；表单设置）
    - 字段设置：字段库列表 → 字段库详情 → 创建字段 → 字段详情/编辑 → 返回（主页仅展示字段库列表）
    - _需求: 40.1, 40.2, 40.3, 13.11, 13.12, 13.13, 13.14_

  - [ ] 15.3 实现表单设置子页面流程
    - 表单列表 → 表单详情 → 创建表单/配置字段 → 表单预览 → 返回（主页仅展示表单列表；详情不展示说明卡）
    - 配置字段：分组/排序/必填/只读/默认值/显示条件/校验覆盖/移除；保存即生效
    - _需求: 40.4, 40.5, 13.15, 13.16, 13.17, 13.18, 13.19, 13.20_

  - [ ] 15.4 实现 `DynamicFormRenderer.vue`
    - 按 `GET /api/forms/{businessObject}/active` 配置动态渲染字段
    - 客户端校验：必填/格式/文件类型/文件大小/敏感字段脱敏/条件显示
    - 供 PR 接收、供应商信息、证件上传、联系人页面复用
    - _需求: 13.6, 13.10, 13.21, 45.4_

- [ ] 16. 前端 - 路由与菜单
  - [ ] 16.1 配置 report/setting 路由与角色守卫
    - 报表/经理工作台/数据设置：ADMIN；采购员工作台/审批中心/邮件日志：BUYER+ADMIN；业务人员首页：BUSINESS_USER；供应商邮件日志：SUPPLIER
    - 各角色登录默认落地页（业务人员→我的采购申请首页）
    - _需求: 36.1, 38.5, 58.1, 53.1, 53.2_

  - [ ] 16.2 同步 `menu.ts` 侧边栏（key=路由 path）
    - 采购经理端新增「供应商管理」「数据设置」一级菜单入口
    - _需求: 38.5, 40.1_

- [ ] 17. 检查点 - 前端完成
  - 确保所有测试通过，如有疑问请向用户确认。

- [ ] 18. 集成与端到端验证
  - [ ] 18.1 共享常量与异常 + HTTP 状态接入
    - `ReportConstants`、`SettingConstants`；新异常 messageCode 加入 `GlobalExceptionHandler.resolveHttpStatus`
    - _需求: 44.1, 45.2_

  - [ ] 18.2 前后端联调验证
    - 报表口径切换与多币种展示、下钻权限不绕过
    - 三类工作台/首页字段裁剪正确
    - 审批中心三 Tab + 深链审批
    - 动态表单在业务页面渲染与提交快照
    - 邮件日志范围隔离与重发
    - 统一编号全局唯一
    - _需求: 36.8, 36.9, 37.4, 39.2, 13.7, 53.2, 42.2_

  - [ ]* 18.3 编写端到端集成测试
    - 报表数据范围隔离、字段库编码唯一、表单保存即生效、审批待办流转
    - _需求: 36.1, 13.2, 13.8, 39.1_

- [ ] 19. 最终检查点 - 全模块完成
  - 确保所有测试通过，如有疑问请向用户确认。

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加速 MVP 交付。
- 每个任务引用对应需求编号以确保可追溯性。
- 报表/工作台为只读聚合，不新建业务表；依赖模块 02–06 表结构稳定。
- 第 13 阶段为「触碰既有模块」的横切集成，按既定约定逐项确认后再改造（编号委托、审批注册、邮件接入、动态表单接入）。
- 邮件真实投递为占位实现（仅落日志），与「外部依赖逐项确认、邮件暂跳过」一致。
- 「采购员账号」页面复用模块 01 用户管理接口，本模块不重复实现账号 CRUD。
