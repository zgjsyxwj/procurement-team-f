# 前端UI应用开发规范（AI协同增强版）v1.0

> **规范定位**
>
> 本规范定义前端研发的最低约束标准，用于统一代码结构、架构边界、工程治理与 AI 协同开发方式。
>
> * `MUST` 条目必须具备自动化检查能力（ESLint / Stylelint / TypeScript / Vitest / CI Gate / Dependency Rules 等）
> * `SHOULD` 条目为强烈建议
> * 无法自动化的 MUST 条目必须纳入 PR Review Checklist
>
> ---
>
> **核心目标**
>
> 1. 让研发人员快速理解系统
> 2. 让 AI Agent 稳定推断代码结构与业务行为
> 3. 降低长期演化成本
> 4. 提高系统可维护性、可测试性与可观测性
> 5. 降低架构熵增
> 6. 降低业务复杂度扩散
> 7. 保持架构可持续演化
>
> ---
>
> **适用范围**
>
> * Vue3 + TypeScript UI 应用
> * 中后台管理系统
> * 企业级 Web 应用
> * BFF 协同前端系统
>
> ---
>
> **默认技术栈**
>
> | 分类    | 推荐                          |
> | ----- | --------------------------- |
> | 框架    | Vue 3                       |
> | 语言    | TypeScript                  |
> | 构建工具  | Vite                        |
> | 状态管理  | Pinia                       |
> | 服务端状态 | TanStack Query              |
> | 路由    | Vue Router                  |
> | HTTP  | Axios                       |
> | UI库   | Element Plus / Naive UI     |
> | 样式    | Tailwind CSS / SCSS Modules |
> | 测试    | Vitest + Playwright         |
> | 监控    | Sentry + OpenTelemetry      |
> | 包管理   | pnpm                        |
> | 状态机   | XState（推荐）                  |
>
> ---
>
> # AI协同开发原则
>
> ## 原则1：显式优于隐式
>
> 禁止：
>
> * 隐式状态共享
> * 运行时魔法注册
> * 动态扫描式业务注入
> * 隐式响应式依赖
> * 深层 Provide/Inject
> * Event Bus 滥用
>
> AI 无法稳定推断隐式行为。
>
> ---
>
> ## 原则2：结构稳定优于灵活抽象
>
> 优先：
>
> * 固定目录
> * 固定命名
> * 固定依赖方向
> * 固定模块结构
>
> 禁止为“高级抽象”牺牲可读性。
>
> ---
>
> ## 原则3：局部上下文完整
>
> 每个模块必须尽量提供完整上下文：
>
> * 明确类型
> * 明确状态来源
> * 明确错误处理
> * 明确依赖关系
> * 明确数据流向
>
> ---
>
> ## 原则4：业务语义优先
>
> 代码首先表达业务含义，而非技术动作。
>
> 正确：
>
> ```ts
> submitOrder()
> cancelPayment()
> approveInvoice()
> ```
>
> 错误：
>
> ```ts
> doAction()
> process()
> handleData()
> ```
>
> ---
>
> ## 原则5：可预测性优先
>
> 相同语义必须使用相同命名模式。
>
> | 语义         | 统一命名           |
> | ---------- | -------------- |
> | 查询         | fetch（动词）      |
> | 创建         | create         |
> | 更新         | update         |
> | 删除         | delete/remove  |
> | 提交         | submit         |
> | 对话框        | XxxDialog      |
> | 表单         | XxxForm        |
> | Store      | useXxxStore    |
> | Composable | useXxx         |
> | UseCase    | xxx.usecase.ts |
> | Mapper     | xxx.mapper.ts  |
>
> 说明：
>
> * 统一使用 `fetch` 进行数据查询
> * `load` 仅用于资源加载
> * `query` 仅用作类型名词
>
> 禁止同义词混用。

---

# 1. 架构规范

## 1.1 默认架构（MUST）

默认采用：

* 模块化单体（Modular Monolith Frontend）
* Component-based SPA
* Composition API
* TypeScript First
* BFF 协同模式
* Frontend DDD 演化结构

默认不使用微前端。

仅在满足以下至少两项时允许拆分：

| 条件     | 描述          |
| ------ | ----------- |
| 独立部署需求 | 发布频率差异 ≥ 3倍 |
| 团队独立   | 存在稳定独立团队    |
| 技术栈异构  | 技术栈无法统一     |
| 性能隔离   | 存在独立性能需求    |

---

## 1.2 架构演化路线（MUST）

| 阶段    | 推荐架构      |
| ----- | --------- |
| 小型项目  | 单体 SPA    |
| 中型项目  | 模块化单体     |
| 大型项目  | BFF + 模块化 |
| 超大型系统 | 微前端（谨慎）   |

微前端是组织架构方案，不是默认技术方案。

---

## 1.3 模块边界（MUST）

模块必须按业务领域划分：

正确：

* order
* payment
* inventory
* user

错误：

* common
* shared-business
* generic-module

---

## 1.4 目录结构（MUST）

```plaintext
src/
├── modules/
│   ├── order/
│   │   ├── application/
│   │   │   ├── submit-order.usecase.ts
│   │   │   ├── cancel-order.usecase.ts
│   │   │   └── approve-order.usecase.ts
│   │   │
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   ├── value-objects/
│   │   │   ├── rules/
│   │   │   └── state-machines/
│   │   │
│   │   ├── infrastructure/
│   │   │   ├── services/
│   │   │   ├── mappers/
│   │   │   ├── adapters/
│   │   │   └── queries/
│   │   │
│   │   ├── presentation/
│   │   │   ├── views/
│   │   │   ├── components/
│   │   │   ├── composables/
│   │   │   ├── stores/
│   │   │   └── routes/
│   │   │
│   │   ├── types/
│   │   │   ├── dto/
│   │   │   ├── vo/
│   │   │   ├── entity/
│   │   │   ├── query/
│   │   │   └── command/
│   │   │
│   │   ├── constants/
│   │   ├── utils/
│   │   └── tests/
│   │
│   └── payment/
│
├── shared/
│   ├── components/
│   ├── composables/
│   ├── utils/
│   ├── directives/
│   ├── types/
│   ├── constants/
│   │
│   └── design-system/
│       ├── tokens/
│       ├── primitives/
│       ├── patterns/
│       └── templates/
│
├── api/
│   ├── client/
│   ├── interceptors/
│   └── types/
│
├── observability/
│   ├── tracing/
│   ├── logging/
│   ├── metrics/
│   └── replay/
│
├── router/
├── store/
├── layouts/
├── plugins/
├── config/
├── assets/
├── docs/
├── App.vue
└── main.ts
```

---

## 1.5 Frontend DDD 分层职责（MUST）

| 层              | 职责      |
| -------------- | ------- |
| presentation   | UI展示与交互 |
| application    | 业务流程编排  |
| domain         | 核心业务规则  |
| infrastructure | 外部系统访问  |

---

## 1.6 分层依赖规则（MUST）

依赖方向必须固定：

```plaintext
presentation
    ↓
application
    ↓
domain
    ↓
infrastructure
```

补充规则：

```plaintext
views
  ↓
components
  ↓
composables
  ↓
stores
  ↓
usecases
  ↓
services
  ↓
api client
```

禁止：

* Service 依赖 Store
* Store 依赖 UI组件
* Components 直接调用 API
* Components 直接操作 localStorage
* Domain 依赖 Vue
* Domain 依赖 HTTP
* 循环依赖

---

## 1.7 模块自治（MUST）

模块必须具备：

* 独立路由
* 独立类型
* 独立错误处理
* 独立状态
* 独立日志前缀
* 独立 Query Key
* 独立状态机

禁止：

* 超级 shared/components
* shared 业务逻辑
* 跨模块复制业务代码

---

## 1.8 Design System 规范（MUST）

`shared/components` 默认仅允许存放基础组件。

Design System 必须分层：

```plaintext
design-system/
├── tokens/
├── primitives/
├── patterns/
└── templates/
```

---

## 1.9 BFF协同规范（SHOULD）

推荐：

```plaintext
Frontend → BFF → Backend Services
```

禁止：

* 前端聚合多个微服务形成事务
* 前端维护复杂权限拼装
* 前端处理跨服务状态一致性

---

# 2. 类型系统规范

## 2.1 TypeScript规范（MUST）

禁止：

* any
* @ts-ignore
* 隐式 any
* 滥用非空断言

必须：

* Props显式类型
* Emits显式类型
* Ref显式类型
* API响应类型

---

## 2.2 领域模型规范（MUST）

| 类型      | 用途     |
| ------- | ------ |
| DTO     | 后端接口模型 |
| VO      | UI展示模型 |
| Entity  | 业务实体   |
| Query   | 查询参数   |
| Command | 写操作参数  |

---

## 2.3 DTO 与 VO 分离（MUST）

默认要求 DTO 与 VO 分离。

统一转换路径：

```plaintext
DTO → Mapper → Entity → VO
```

禁止 UI 直接依赖后端 DTO。

---

## 2.4 Mapper规范（MUST）

所有 DTO 转换必须进入：

```plaintext
mappers/
```

禁止：

* 页面中直接转换 DTO
* Components 中拼装 Entity
* 重复字段映射

---

# 3. 状态管理规范

## 3.1 状态分类（MUST）

| 状态类型  | 放置位置           |
| ----- | -------------- |
| UI状态  | 组件内部           |
| 页面状态  | composables    |
| 业务状态  | store          |
| 服务端缓存 | query          |
| 临时状态  | ref/reactive   |
| 状态机状态 | state-machines |

禁止：

* 所有状态进入 Pinia
* Store 成为全局垃圾桶

---

## 3.2 Store规范（MUST）

Store：

* 只保存业务状态
* 不保存 UI 状态
* 不直接操作 DOM
* 不直接依赖组件

---

## 3.3 服务端状态管理（MUST）

统一使用：

* TanStack Query

管理：

* cache
* retry
* staleTime
* request dedupe
* optimistic update

---

## 3.4 Query Key规范（MUST）

Query Key 必须集中管理：

```plaintext
query-keys/
```

示例：

```ts
orderKeys.list(params)
orderKeys.detail(id)
```

禁止：

* 字符串硬编码 query key
* 页面内直接拼接 query key

---

## 3.5 缓存一致性规范（MUST）

必须定义：

* invalidate策略
* optimistic rollback
* staleTime分级
* cache生命周期

---

# 4. 状态机规范

## 4.1 必须使用状态机的场景（MUST）

满足以下任意条件必须使用状态机：

* 多步骤流程
* 长事务
* 支付流程
* 审批流程
* 上传流程
* 可恢复流程
* 并行状态
* 重试机制

---

## 4.2 状态机规范（MUST）

状态机必须：

* 显式状态定义
* 显式 transition
* 可视化状态图
* 可测试

禁止：

* if/else 状态流
* 隐式状态切换

---

# 5. Composable规范

## 5.1 命名（MUST）

```plaintext
useXxx.ts
```

---

## 5.2 分类（MUST）

| 类型               | 职责    |
| ---------------- | ----- |
| useXxxQuery      | 服务端状态 |
| useXxxMutation   | 写操作   |
| useXxxForm       | 表单逻辑  |
| useXxxTable      | 表格逻辑  |
| useXxxPermission | 权限逻辑  |
| useXxxPage       | 页面编排  |

禁止：

* useEverything.ts
* Composable 承担 Domain职责
* Composable 直接操作 HTTP

---

## 5.3 副作用清理（MUST）

必须清理：

* timer
* event listener
* request
* observer

---

# 6. UseCase规范

## 6.1 UseCase职责（MUST）

UseCase负责：

* 业务流程编排
* 权限校验
* 状态协调
* 错误恢复
* 调用顺序控制

禁止：

* Components 编排业务流程
* Store 编排业务流程

---

## 6.2 UseCase命名（MUST）

```plaintext
submit-order.usecase.ts
approve-payment.usecase.ts
```

---

# 7. 组件规范

## 7.1 组件原则（MUST）

* 单一职责
* 无副作用
* 可组合
* 可测试

---

## 7.2 组件大小限制（MUST）

| 类型   | 限制     |
| ---- | ------ |
| 普通组件 | ≤ 300行 |
| 页面组件 | ≤ 500行 |

---

## 7.3 Props规范（MUST）

* 显式类型
* 非必填提供默认值
* 复杂对象使用工厂函数

---

## 7.4 Emits规范（MUST）

必须：

* 显式声明
* 明确 payload 类型

---

## 7.5 表单组件规范（MUST）

必须支持：

* v-model
* disabled
* loading
* error state
* validation

---

# 8. API与数据访问规范

## 8.1 Service层规范（MUST）

Service：

* 无状态
* 不依赖 Store
* 不依赖 UI
* 仅负责数据请求

---

## 8.2 Adapter规范（MUST）

Adapter负责：

* 后端兼容
* 字段适配
* 历史接口兼容
* 数据格式转换

禁止：

* UI层兼容后端历史字段

---

## 8.3 API错误处理（MUST）

必须统一处理：

* 401
* 403
* 500
* 网络超时

---

## 8.4 请求取消（MUST）

必须支持：

* AbortController
* 页面切换自动取消

---

# 9. 路由规范

## 9.1 路由规范（MUST）

必须：

* 路由懒加载
* 唯一路由名称
* meta.title
* requiresAuth

---

## 9.2 权限路由（MUST）

权限逻辑必须：

* 显式声明
* 可测试
* 不允许散落在组件中

---

# 10. 样式规范

## 10.1 默认方案（MUST）

推荐：

* Tailwind CSS
* SCSS Modules
* Scoped CSS

---

## 10.2 禁止事项（MUST）

禁止：

* !important 滥用
* 全局污染
* 大量内联样式

---

## 10.3 Design Token（MUST）

必须统一：

* spacing
* color
* typography
* radius
* shadow
* z-index

禁止硬编码视觉参数。

---

# 11. 性能规范

## 11.1 必须优化项（MUST）

必须：

* 路由懒加载
* 图片懒加载
* 长列表虚拟滚动
* 高频事件防抖/节流

---

## 11.2 响应式优化（MUST）

推荐：

* computed
* shallowRef
* shallowReactive

禁止：

* 模板中复杂计算

---

## 11.3 包体积治理（MUST）

必须：

* Tree Shaking
* 按需加载
* Bundle 分析

---

# 12. 安全规范

## 12.1 XSS防御（MUST）

禁止：

* 未清洗 v-html

必须：

* DOMPurify

---

## 12.2 Token规范（MUST）

优先使用：

* httpOnly Cookie

localStorage 存储 token 必须经过安全评审。

---

## 12.3 CSP规范（SHOULD）

推荐：

* Content Security Policy
* script-src 白名单

---

## 12.4 权限控制（MUST）

权限控制必须：

* 路由层校验
* 接口层校验
* 组件层校验

---

# 13. 可观测性规范

## 13.1 日志规范（MUST）

必须：

* 结构化日志
* traceId
* 用户上下文

---

## 13.2 OpenTelemetry规范（SHOULD）

推荐：

* trace propagation
* frontend span
* distributed tracing

---

## 13.3 Session Replay（SHOULD）

推荐：

* 用户行为回放
* 错误上下文关联

---

## 13.4 性能监控（MUST）

必须监控：

* LCP
* CLS
* FID
* API latency
* JS Error

---

# 14. 测试规范

## 14.1 测试分层（MUST）

| 类型          | 工具             |
| ----------- | -------------- |
| Unit        | Vitest         |
| Component   | Vue Test Utils |
| Integration | MSW + Vitest   |
| E2E         | Playwright     |

---

## 14.2 覆盖率要求（MUST）

| 类型      | 要求    |
| ------- | ----- |
| 核心业务逻辑  | ≥ 80% |
| 核心组件    | ≥ 70% |
| 核心流程E2E | 100%  |

---

## 14.3 状态机测试（MUST）

状态机必须覆盖：

* transition
* rollback
* retry
* timeout

---

# 15. 可访问性与国际化

## 15.1 可访问性（MUST）

必须支持：

* aria-label
* 键盘导航
* 语义化HTML

---

## 15.2 国际化（MUST）

必须：

* UI 文本禁止硬编码
* 使用 i18n key

---

# 16. CI/CD规范

## 16.1 CI门禁（MUST）

必须通过：

* Type Check
* ESLint
* Stylelint
* Unit Test
* Build
* Dependency Rules

---

## 16.2 依赖方向校验（MUST）

必须使用：

* eslint-plugin-boundaries
* dependency-cruiser
* madge

检查：

* 循环依赖
* 越层依赖
* 非法 import

---

## 16.3 发布规范（MUST）

必须支持：

* 灰度发布
* 回滚
* CDN部署
* SourceMap上传

---

# 17. Monorepo规范

## 17.1 默认方案（SHOULD）

推荐：

```plaintext
pnpm workspace
```

---

## 17.2 包划分（SHOULD）

```plaintext
apps/
packages/
```

---

# 18. AI协同开发规范

## 18.1 AI可读工程（MUST）

docs目录必须包含：

```plaintext
docs/
├── glossary.md
├── architecture.md
├── component-map.md
├── api-mapping.md
├── state-flow.md
├── state-machines.md
├── query-strategy.md
└── coding-rules.md
```

---

## 18.2 AI禁止事项（MUST）

以下内容必须人工 Review：

* 权限逻辑
* 金额计算
* 支付逻辑
* 用户隐私
* 状态机
* 安全逻辑

---

## 18.3 AI生成代码要求（MUST）

AI代码必须：

* 通过 Type Check
* 通过 ESLint
* 补充测试
* 补充注释
* 符合目录规范

---

# 19. 技术债治理

## 19.1 TODO规范（MUST）

```ts
// TODO(@user): 描述问题，计划版本 v2.0
// FIXME(@user): 描述问题，截止日期 2026-07-01
```

---

## 19.2 依赖治理（MUST）

每月：

* npm audit
* depcheck
* 依赖升级检查

---

## 19.3 性能审计（MUST）

每季度：

* Lighthouse ≥ 90
* 首屏 < 2s
* Bundle Size检查

---

# 20. 禁止清单（MUST）

禁止：

* 巨型组件
* Components 直接请求 API
* Store 成为全局垃圾桶
* any 类型
* 循环依赖
* 魔法字符串
* 深层 Provide/Inject
* Event Bus 滥用
* 模板复杂逻辑
* shared 业务逻辑
* Service 持有状态
* 页面直接拼装 DTO
* Store 编排业务流程
* Components 编排业务流程
* 隐式状态流转

---

# 21. 规范执行机制

## 21.1 自动化优先（MUST）

所有 MUST 条目优先自动化校验。

---

## 21.2 PR Review Checklist（MUST）

PR 必须检查：

* 命名规范
* 类型完整性
* 测试覆盖
* 错误处理
* 性能问题
* 安全问题
* 是否符合依赖方向
* 是否破坏状态边界
* 是否破坏缓存一致性

---

## 21.3 架构委员会治理（SHOULD）

建议：

* 每季度架构审查
* 每半年规范升级
* 建立技术债看板

---

**规范版本**：v1.0
**规范状态**：正式版
**适用范围**：所有前端 UI 应用
**所有者**：前端架构委员会
**生效日期**：2026-06-01
**对应后端规范版本**：v1.0
