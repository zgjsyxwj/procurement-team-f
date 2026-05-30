# Requirements Document

## Introduction

履约与付款模块负责业务人员基于已签署合同发起采购订单（PO）、PO 审批（对接 Worklife 系统 BPM 模块）、PO 同步台账管理系统、付款结果回传、PO 列表与详情追踪。支持一份合同拆分多个 PO，区分费用类 PO 和业务类 PO。

### PO 状态流转

```
PO草稿(Draft)
  → 审批中(Approving)            [业务人员提交 PO 至 Worklife BPM]
  → 审批驳回(Rejected)           [Worklife BPM 审批驳回]
  → 审批通过(Approved)           [Worklife BPM 审批通过]

台账同步状态：
  未同步(Not_Synced) → 已同步(Synced) / 同步失败(Sync_Failed)

付款状态：
  待付款(Pending_Payment) → 部分付款(Partial_Paid) → 已付款(Fully_Paid)
```

## Glossary

本模块术语沿用主文档（procurement-platform/requirements.md）Glossary 章节定义。

## Requirements

### Requirement 33: 业务人员发起 PO

**User Story:** 作为业务人员，我希望合同签署完成后可在该笔合同上创建 PO，并提交审批，以便审批通过后进入真实付款。

#### Acceptance Criteria

1. WHEN 合同状态为"合同完成"后, THE 系统 SHALL 仅允许该 PR 的原始申请人基于该合同发起 PO；其他业务人员不可发起该 PR 下的 PO
2. THE 系统 SHALL 自动带入 PR 信息、合同信息、中标供应商、最终中标价和付款条款
3. THE 系统 SHALL 支持一份 PR/合同拆分为多个 PO，每个 PO 需维护拆分金额和归集对象
4. WHEN PO 为业务类 PO 时, THE 系统 SHALL 要求关联销售订单
5. WHEN PO 为费用类 PO 时, THE 系统 SHALL 要求关联成本中心
6. WHEN 业务人员提交 PO 时, THE 系统 SHALL 调用 Worklife 系统 BPM 模块创建 PO 审批流程
7. WHEN PO 审批通过后, THE 系统 SHALL 将 PO审批状态设置为"审批通过"，并触发同步台账系统
8. WHEN PO 审批驳回后, THE 系统 SHALL 允许业务人员查看驳回原因后修改重新提交
9. THE 系统 SHALL 接收 Worklife 系统 BPM 模块 PO 审批回调，保存审批单号、审批状态、审批意见、审批完成时间和审批操作人
10. THE 系统 SHALL 将 PO审批状态、台账同步状态、付款状态同步展示在采购申请单列表和业务人员端 PR/合同详情中，三类状态不得混用
11. THE 系统 SHALL 记录 PO 创建业务人员、PR 申请人、合同、供应商、成本中心或销售订单快照，用于审批、台账同步和审计追溯

### Requirement 34: PO 同步台账系统与付款回传

**User Story:** 作为财务协同方，我希望采购系统将 PO 同步至台账管理系统并接收付款结果，以便完成付款闭环。

#### Acceptance Criteria

1. WHEN PO 第三方审批通过后, THE 系统 SHALL 将 PO 信息同步至台账管理系统
2. 同步内容 SHALL 包含：PO编号、PR编号、合同编号、供应商、金额、币种、付款条款、PO类型、成本中心或销售订单、明细行
3. WHEN 台账系统接收成功后, THE 系统 SHALL 将台账同步状态更新为"已同步"，付款状态更新为"待付款"
4. WHEN 台账系统接收失败后, THE 系统 SHALL 将台账同步状态更新为"同步失败"，记录失败原因，并支持重试
5. WHEN 台账系统完成付款节点变化时, THE 系统 SHALL 接收付款状态回传，更新 PO 付款状态、已付款金额、付款时间和付款流水号
6. THE 系统 SHALL 以台账系统回传结果作为付款状态和付款金额的唯一可信来源
7. THE 系统 SHALL 将付款状态回写至采购申请单列表

### Requirement 35: PO 列表与详情追踪

**User Story:** 作为业务人员、采购员或采购经理，我希望在 PO 列表和详情中清晰查看 PO 审批、台账同步和付款进度。

#### Acceptance Criteria

1. THE 系统 SHALL 提供 PO 列表页面，展示 PO编号、关联PR、关联合同、供应商、PO金额、货币、PO审批状态、台账同步状态、付款状态、已付款金额
2. THE PO 详情页 SHALL 展示 PO 基本信息、审批信息、台账同步结果、付款回传记录、PO 明细
3. THE 系统 SHALL 保持 PO审批状态、台账同步状态、付款状态三类状态独立展示和更新
4. WHEN 一个 PR 关联多个 PO 时, THE 系统 SHALL 在 PR 详情「PO与付款」Tab 汇总展示多个 PO 的付款结果
5. THE 系统 SHALL 仅在 PO 审批通过后同步台账系统；审批中或驳回的 PO 不得同步台账
6. THE PO 详情页 SHALL 展示 PO 基本信息、审批信息、台账同步结果、付款回传记录、PO 明细、成本中心或销售订单信息；不展示「系统同步模块」说明卡，不展示 PO 状态流程说明卡
7. THE PO 详情页 SHALL 将关联 PR 号展示为可点击链接，点击后进入对应 PR 详情页

### Requirement 52: PO 审批规则

**User Story:** 作为采购经理，我希望 PO 是否需要审批有清晰规则，以便控制超合同范围的付款风险。

#### Acceptance Criteria

1. THE 系统 SHALL 将业务人员提交的 PO 统一推送至 Worklife BPM 审批
2. IF PO 内容的价格、数量和金额在已签署合同约定范围内, THEN Worklife BPM 可按低风险规则自动或快速审批，采购系统仅接收审批结果
3. IF PO 内容超出原合同约定范围, THEN Worklife BPM 应按高风险规则审批，采购系统需在审批提交前提示超合同风险
4. THE 系统 SHALL 仅在 PO 审批通过后同步台账系统；审批中、审批驳回、审批撤回的 PO 不得同步台账

### Requirement 48: PO 取消流程

**User Story:** 作为 PR 原始申请人，我希望在 PO 不再需要时按状态取消或撤回 PO，以便避免错误付款流入台账。

#### Acceptance Criteria

1. WHEN PO 处于"草稿"状态时, THE 系统 SHALL 允许 PR 原始申请人直接取消 PO，并记录取消原因、操作人和操作时间
2. WHEN PO 处于"审批驳回"状态时, THE 系统 SHALL 允许 PR 原始申请人取消 PO，或修改后重新提交
3. WHEN PO 处于"审批中"状态时, THE 系统 SHALL 不允许采购系统直接取消 PO，必须发起 Worklife BPM 撤回；撤回成功后 PO审批状态变为"已取消"
4. WHEN PO 处于"审批通过"但尚未同步台账时, THE 系统 SHALL 允许 PR 原始申请人申请取消，经采购经理/Admin 确认后将 PO审批状态和台账同步状态变更为"已取消"
5. WHEN PO 已同步台账后, THE 系统 SHALL 不允许在采购系统直接取消，只能发起台账作废或冲销流程
6. WHEN PO 已付款后, THE 系统 SHALL 不允许取消 PO，只能保留付款记录并通过财务冲销流程处理
7. WHEN PO 被取消后, THE 系统 SHALL 将取消结果同步展示在 PR 详情「PO与付款」Tab 和 PR 列表相关状态字段中

## 依赖关系

- 依赖模块 01（用户认证与权限）
- 依赖模块 03（采购申请单）：PO 关联 PR
- 依赖模块 05（合同管理）：PO 基于已完成合同发起
- 依赖外部系统：Worklife 系统 BPM 模块（PO 审批）、台账管理系统（同步与付款回传）
