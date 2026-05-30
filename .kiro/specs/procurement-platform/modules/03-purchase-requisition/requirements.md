# Requirements Document

## Introduction

采购申请单模块负责业务人员发起 PR、Worklife 系统 BPM 模块审批与采购员分配、业务人员进度跟踪、采购员/采购经理接收 PR、PR 合集管理、PR 与下游单据（RFQ/合同/PO/付款）的关联追踪。PR 是采购系统当前版本的流程起点。

### PR 主状态机

```
草稿(Draft)
  → 审批中(Approving)            [业务人员提交 PR 至 Worklife 系统 BPM 模块]
  → 审批驳回(Rejected)           [Worklife BPM 审批驳回，业务人员可修改重提]
  → 待询价(Pending_RFQ)          [审批通过并完成采购员分配]
  → 询价中(RFQ_In_Progress)      [已创建 RFQ]
  → 待核价(Price_Review_Pending) [报价截止并开标]
  → 核价审批中(Price_Review_Approving) [核价提交采购经理审批]
  → 核价退回(Price_Review_Rejected)    [采购经理驳回核价]
  → 待建合同(Pending_Contract)   [核价审批通过]
  → 采购流程完成(Procurement_Completed) [合同签署归档完成]
  → 已取消(Cancelled)            [PR 被取消或关闭]
```

## Glossary

本模块术语沿用主文档（procurement-platform/requirements.md）Glossary 章节定义。

## Requirements

### Requirement 14: 业务人员发起采购申请单

**User Story:** 作为业务人员，我希望在采购系统内发起采购申请单，并提交到 Worklife 系统 BPM 模块审批，以便采购需求审批通过后进入采购流程。

#### Acceptance Criteria

1. THE 系统 SHALL 提供业务人员端采购申请单创建页面，允许业务人员填写并提交 PR
2. WHEN 业务人员创建 PR 时, THE 系统 SHALL 支持填写以下字段：申请日期、部门、主题、产品/服务需求、是否唯一供应商、附件、申请人、成本中心、确认采购不涉及社保挂靠或代缴、预算明细、采购类型
3. WHEN 「是否唯一供应商」选择「是」时, THE 系统 SHALL 展示并要求填写「供应商类型」（单选必填：市场部活动相关供应商、客户指定供应商、已有设备指定供应商、其他）；选择「否」时隐藏该字段
4. THE 预算明细 SHALL 支持多行录入，字段包含序号、产品/服务需求、采购数量、单位、采购单价、币种、总价、备注；总价和预算总额由系统自动计算
5. THE 采购类型 SHALL 支持公司费用类采购和业务成本类采购；成本中心为 PR 基础必填字段，公司费用类采购以成本中心作为费用归集依据
6. WHEN 「采购类型」选择「业务成本类采购」时, THE 系统 SHALL 展示并要求填写客户名称（文本必填）、CDP公司抬头（单选必填，来自公司抬头主数据）、上传双盖章销售合同（附件必填）、CRM合同编号/订单编号（文本必填）、该采购预算是否体现在项目利润表中（单选必填，是/否）
7. WHEN 业务人员提交 PR 时, THE 系统 SHALL 将 PR 状态设置为"审批中"，并调用 Worklife 系统 BPM 模块创建审批流程
8. THE 系统 SHALL 接收 Worklife 系统 BPM 模块回调，保存审批单号、审批状态、审批意见、审批完成时间和审批操作人
9. WHEN Worklife BPM 审批通过时, THE 系统 SHALL 要求回调数据包含被分配采购员信息
10. WHEN PR 审批通过且已分配采购员后, THE 系统 SHALL 将 PR 状态设置为"待询价"
11. WHEN PR 审批驳回时, THE 系统 SHALL 将 PR 状态设置为"审批驳回"，并允许业务人员修改重新提交
12. THE 系统 SHALL 确保 PR 号唯一，PR号统一由采购系统在提交时按 PR-YYYYMM-5位自增规则生成；Worklife BPM 回传的审批单号保存为 externalApprovalNo，不得覆盖 PR号
13. THE PR Received Date SHALL 取 PR 进入采购系统并落库的时间
14. THE Enduser 和 Department head 字段 SHALL 当前由外部或组织数据带出展示，后续可通过 PR 表单字段配置改为业务人员填写字段
15. WHEN 业务人员在 PR 创建页点击预算明细「添加行」时, THE 系统 SHALL 展示可编辑的预算明细行或弹窗表单，字段包含产品/服务需求、采购数量、单位、采购单价、总价和备注，并支持保存、取消和删除行
16. THE 业务人员端 PR 创建页 SHALL 只展示业务填写、预算明细、附件和提交操作，不展示「第三方审批与分配规则」类说明模块

### Requirement 15: 采购申请单跟踪与管理

**User Story:** 作为业务人员和采购员，我希望查看和管理采购申请单的进度，以便跟踪采购流程各阶段状态。

#### Acceptance Criteria

1. THE 系统 SHALL 支持业务人员查看本人发起的 PR 列表和详情，包含审批状态、分配采购员、合同、PO和付款进度；业务人员端不展示 RFQ 入口、RFQ Tab 或 RFQ 编号
2. THE 系统 SHALL 支持被分配采购员查看被分配给自己的 PR，并按采购申请单号、主题、申请人、申请部门、PR单状态进行搜索和筛选
3. THE 系统 SHALL 支持采购经理查看全量 PR 列表
4. THE 采购申请单列表 SHALL 展示 PR 基础信息、PR状态、合同状态、PO审批状态、台账同步状态、付款状态、已付款总额
5. WHEN 采购员或采购经理进入 PR 详情页时, THE 系统 SHALL 通过 Tab 展示 PR概览、RFQ、合同、PO与付款信息；WHEN 业务人员进入 PR 跟踪详情页时, THE 系统 SHALL 仅展示 PR概览、合同、PO与付款信息，不展示 RFQ Tab
6. THE 系统 SHALL 支持一个 PR 关联多个 PO；关联 PO 信息在 PR 详情「PO与付款」Tab 展示
7. WHEN 一个 PR 或 PR 合集下所有未取消的有效 RFQ 已完成核价、所有基于这些 RFQ 创建的合同均已签署归档，且不存在进行中的 RFQ/核价/合同流程时, THE 系统 SHALL 将关联 PR 主状态自动更新为"采购流程完成"；PR 不得因第一份合同完成而提前完成
8. THE 系统 SHALL 不允许手动绕过未完成 RFQ/合同流程直接将 PR 标记为"采购流程完成"；如需终止流程，应使用取消/关闭 PR 操作
9. WHEN PR 被取消或关闭时, THE 系统 SHALL 联动取消该 PR 下仍可取消的 RFQ、合同和 PO；若存在已同步台账或已付款 PO，系统 SHALL 阻止直接取消 PR
10. THE 系统 SHALL 支持业务人员取消处于"草稿"或"审批驳回"状态的 PR；PR 已进入采购流程后，仅被分配采购员或采购经理可取消或关闭 PR
11. THE 业务人员端采购申请单跟踪页 SHALL 不展示「流程跟踪」说明模块；页面应以状态标签、时间节点和下游单据列表表达进度
12. THE 采购员和采购经理端 PR 列表搜索栏 SHALL 包含采购申请单号、主题、申请人、申请部门、PR单状态五类筛选项；合同状态、PO审批状态、台账同步状态和付款状态仅作为展示或详情追踪字段，不作为 PR 列表搜索项
13. THE 采购申请单列表 SHALL 不在列表中展示具体 PO 编号，也不在列表操作列直接展示"查看RFQ"、"查看合同"、"查看PO"；具体 PO 编号在 PR 详情「PO与付款」Tab 中查看
14. THE 系统 SHALL 在 PR 取消或关闭确认前展示受影响的 RFQ、合同、PO 清单，用户确认后才可执行
15. WHEN PR 取消或关闭通过校验后, THE 系统 SHALL 将 PR 主状态设置为"已取消"，并将所有仍可取消的下游 RFQ、合同和 PO 状态同步变更为"已取消"；IF 某下游对象缺少"已取消"状态, THEN THE 系统 SHALL 在对应状态域新增该状态

### Requirement 16: 采购申请单合集

**User Story:** 作为采购员，我希望将多张已审批通过的 PR 合并为一张合集，以便统一进入询价、合同和付款流程。

#### Acceptance Criteria

1. THE 系统 SHALL 支持普通采购员将多张审批通过、已分配给本人且尚未创建 RFQ/合同/PO 的 PR 合并为一张 PR 合集；采购经理/Admin 可跨采购员合并 PR
2. WHEN 创建 PR 合集时, THE 系统 SHALL 要求选择至少两张 PR，并填写合集名称、希望完成时间和合并原因；采购经理/Admin 跨采购员合并时必须指定合集负责采购员；合集预算等于所选 PR 预算合计
3. WHEN PR 合集创建成功后, THE 系统 SHALL 将该合集作为可创建 RFQ 的来源；合集仅由负责采购员和采购经理/Admin 可见和操作
4. THE 系统 SHALL 防止已合并的原始 PR 再次单独创建 RFQ
5. WHEN PR 合集尚未生成询价单时, THE 系统 SHALL 允许采购员移除某一条原始 PR，并要求填写移除原因
6. WHEN 原始 PR 从 PR 合集中移除后, THE 系统 SHALL 重新计算合集预算，并将被移除 PR 恢复为可单独使用的状态
7. IF PR 合集已经生成询价单, THEN THE 系统 SHALL 冻结合集明细，不允许移除合集内原始 PR
8. IF 原始 PR 已被加入 PR 合集, THEN THE 系统 SHALL 禁止用户手动完成或单独推进该原始 PR，并提示"该PR单已被整合，请在PR合集流程中处理"
9. THE 系统 SHALL 禁止用户手动点击完成 PR 合集；PR 合集在满足所有有效 RFQ/合同完成条件后由系统自动计算为"采购流程完成"

### Requirement 51: PR 主状态联动规则

**User Story:** 作为系统管理员，我希望 PR 主状态随下游流程进展自动流转，以便准确反映采购流程当前阶段。

#### Acceptance Criteria

1. THE 系统 SHALL 按以下规则自动联动 PR 主状态：Worklife BPM 审批通过并分配采购员→待询价；创建并发布有效 RFQ→询价中；RFQ 报价截止并开标→待核价；提交核价审批→核价审批中；核价审批驳回→核价退回；核价审批通过→待建合同；所有有效 RFQ/合同均完成归档且无进行中流程→采购流程完成；PR 被取消或关闭→已取消
2. THE PR 主状态 SHALL 仅表示采购申请单自身及采购流程阶段；合同状态、PO审批状态、台账同步状态、付款状态均为独立状态字段，不写入 PR 主状态
3. THE PR 主状态"采购流程完成" SHALL 表示采购流程完成，不代表付款完成；PR 列表和报表仍展示 PO审批状态、台账同步状态、付款状态和已付款总额
4. THE 系统 SHALL 不允许用合同、PO、台账或付款状态覆盖 PR 主状态

## 依赖关系

- 依赖模块 01（用户认证与权限）
- 依赖外部系统：Worklife 系统 BPM 模块（PR 审批回调）
- 被模块 04（询报价管理）依赖：RFQ 创建时需选择 PR/PR 合集
- 被模块 05（合同管理）依赖：合同创建时需选择 PR
- 被模块 06（履约与付款）依赖：PO 关联 PR
