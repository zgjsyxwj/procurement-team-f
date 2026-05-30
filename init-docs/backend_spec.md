# 后端服务开发规范（AI协同增强版）v1.0

> **规范定位**
> 本规范定义后端研发的最低约束标准，用于统一代码结构、架构边界、工程治理与AI协同开发方式。
> - `MUST` 条目必须具备自动化检查能力（ArchUnit / SonarQube / Checkstyle / CI Gate 等），若暂时无法自动化，则必须在团队 PR 评审中强制校验，且后续需引入工具。
> - `SHOULD` 条目为强烈建议，鼓励但非强制，可作为技术债跟踪。
>
> **核心目标**
>
> 1. 让研发人员快速理解系统
> 2. 让 AI Agent 可稳定推断代码结构与行为
> 3. 降低系统长期演化成本
> 4. 提高代码可维护性、可观测性与可测试性
>
> ---
>
> **AI协同开发原则**
>
> ### 原则1：显式优于隐式
>
> 禁止依赖隐藏行为、运行时魔法、动态注入、隐式上下文。
>
> AI对如下内容理解能力较弱，应谨慎使用：
>
> * 反射
> * 复杂AOP
> * 动态Bean注册
> * 运行时字节码增强
> * 隐式ThreadLocal状态
> * 大量条件化配置
>
> ---
>
> ### 原则2：结构稳定优于灵活抽象
>
> 优先稳定目录结构与固定模式，而非“高度抽象”。
>
> AI对稳定结构学习效果最佳：
>
> * 固定命名
> * 固定目录
> * 固定层级
> * 固定职责
>
> ---
>
> ### 原则3：局部上下文完整
>
> 单个类/模块应尽量提供完整上下文，不依赖全局推断。
>
> 包括：
>
> * 明确类型
> * 明确DTO
> * 明确边界
> * 明确依赖
> * 明确错误
>
> ---
>
> ### 原则4：业务语义优先
>
> 代码首先表达业务，而不是技术。
>
> 示例：
>
> 好：
>
> ```java
> approveOrder()
> reserveInventory()
> cancelPayment()
> ```
>
> 差：
>
> ```java
> updateStatus()
> process()
> execute()
> handle()
> ```
>
> ---
>
> ### 原则5：可预测性优先
>
> 相同语义必须使用相同命名与实现模式。
>
> 例如：
>
> * 创建统一使用 `create`
> * 查询统一使用 `get/find/query`
> * 删除统一使用 `delete/remove`
> * DTO统一后缀 `Request/Response`
> * Command统一后缀 `Command`
>
> 禁止同义词混用。

---

# 1. 架构原则

## 1.1 默认架构（MUST）

默认采用：

* **Modular Monolith（模块化单体）**
* **DDD Lite**
* **六边形架构（Hexagonal Architecture）**
* **CQRS（轻量化）**

默认不拆微服务。

仅在满足以下至少两项时允许拆分：

| 条件   | 描述          |
| ---- | ----------- |
| 独立扩容 | 资源需求差异 > 5倍 |
| 独立发布 | 发布频率差异 > 3倍 |
| 团队边界 | 存在稳定独立团队    |
| 数据隔离 | 存在合规或租户隔离需求 |

---

## 1.2 服务边界（MUST）

服务拆分必须按业务能力进行。

正确示例：

* 订单
* 库存
* 支付
* 用户

错误示例：

* ServiceA
* UserCommon
* DataCenter

---

## 1.3 服务间数据隔离（MUST）

**【修订】** 服务之间必须严格隔离数据：

- 禁止共享数据库 Schema
- 禁止跨服务直接查询表
- 禁止跨服务事务

**模块化单体内部的例外**（仅在单体架构且模块属于同一服务边界时允许）：
- 允许模块共享同一数据库，但必须满足：
  - 不同模块使用独立表前缀（如 `order_`、`user_`）或独立 Schema
  - 禁止跨模块直接 SQL 查询（必须通过 application 层调用）
  - 禁止跨模块数据库事务（必须使用最终一致性或 Outbox）
- 若后续拆分为微服务，必须立即拆库或增加数据同步层。

跨服务数据交互只能通过：

* REST
* RPC
* MQ
* CDC/Event

---

## 1.4 模块结构（MUST）

目录结构必须统一。

```plaintext
src/main/java/com/cdp/ecosaas/{product}/{service}/
├── domain/
│   ├── model/
│   ├── service/           # 领域服务
│   ├── repository/        # 仓储接口（端口）
│   ├── port/              # 其他端口（如外部服务、消息等接口）
│   ├── event/
│   └── rule/
│
├── application/
│   ├── command/
│   ├── query/
│   ├── handler/
│   ├── assembler/
│   └── service/           # 应用服务（用例编排）
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   ├── repository/    # 仓储实现
│   │   └── mapper/
│   ├── rpc/
│   ├── mq/
│   ├── cache/
│   ├── config/
│   ├── web/
│   └── schedule/
│
├── interfaces/
│   ├── rest/
│   ├── dto/
│   └── converter/
│
├── shared/
│   ├── constants/
│   ├── enums/
│   ├── exception/
│   ├── utils/
│   └── response/
│
└── bootstrap/
```

---

## 1.5 分层职责（MUST）

| 层              | 职责     |
| -------------- | ------ |
| domain         | 核心业务规则 |
| application    | 用例编排   |
| infrastructure | 技术实现   |
| interfaces     | API适配  |
| shared         | 通用基础能力 |
| bootstrap      | 启动装配   |

---

## 1.6 分层依赖规则（MUST）

依赖方向必须固定：

```plaintext
interfaces → application → domain
infrastructure → domain（实现端口）
infrastructure 可以依赖 application（仅当需要调用应用服务时，但应尽量避免）
domain → 无外部依赖（包括 Spring、ORM、HTTP 等）
```

禁止：
- domain 依赖 Spring / ORM / HTTP 客户端
- controller 直接操作 repository
- application 直接写 SQL

---

## 1.7 模块自治（MUST）

模块必须具备：

* 独立DTO
* 独立数据库表
* 独立领域对象
* 独立错误码
* 独立日志前缀

禁止：

* “common业务模块”
* 巨型shared包
* 跨模块复制调用链

---

# 2. 编码规范

## 2.1 命名规范（MUST）

### 类命名

| 类型         | 后缀         |
| ---------- | ---------- |
| Controller | Controller |
| Service    | Service    |
| Repository | Repository |
| Command    | Command    |
| Query      | Query      |
| Request    | Request    |
| Response   | Response   |
| Entity     | Entity     |
| Assembler  | Assembler  |

---

### 方法命名

| 语义   | 前缀             |
| ---- | -------------- |
| 创建   | create         |
| 更新   | update         |
| 删除   | delete         |
| 查询   | get/find/query |
| 校验   | validate       |
| 转换   | convert/to     |
| 发布事件 | publish        |

---

### 布尔命名（MUST）

统一使用：

* isXxx
* hasXxx
* canXxx
* shouldXxx

禁止：

```java
success()
flag()
status()
```

---

## 2.2 DTO规范（MUST）

禁止：

* Map传参
* Object传参
* JsonNode传参
* 动态结构

必须：

* 显式DTO
* 显式字段
* 显式校验

正确：

```java
CreateOrderRequest
CreateOrderResponse
```

错误：

```java
Map<String, Object>
```

---

## 2.3 方法复杂度（MUST）

| 指标   | 限制        |
| ---- | --------- |
| 圈复杂度 | ≤ 10      |
| 嵌套深度 | ≤ 3       |
| 方法参数 | ≤ 3（建议） |
| 方法长度 | ≤ 30 行    |

**【修订】** 详细规则：

- **方法参数**：推荐 ≤ 3。若超过3个，应使用参数对象（如 `XxxQuery`、`XxxContext`）封装，且禁止零散基本类型超过3个（如 `String a, String b, int c, long d`）。此为 SHOULD 级别，不强制自动化检查，但PR时需评审。
- **方法长度**：超过30行必须拆分。如有合理理由（如复杂映射构建、状态机转换、数据组装），可在PR中注释说明原因，由评审人特批豁免，豁免次数应作为技术债跟踪。
- **圈复杂度与嵌套深度**：严格 MUST，超过须拆分。

---

## 2.4 禁止过度抽象（MUST）

禁止：

* BaseController
* BaseService
* BaseRepository
* GenericCrudService<T>
* 超过3层继承

优先：

* 组合
* 显式实现
* 业务语义

---

## 2.5 构造器注入（MUST）

统一使用构造器注入。

禁止：

```java
@Autowired
private OrderService orderService;
```

正确：

```java
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
}
```

---

## 2.6 Lombok使用规范（MUST）

允许：

* `@Getter` / `@Setter`
* `@Builder`
* `@RequiredArgsConstructor`
* `@Slf4j`
* `@EqualsAndHashCode`（仅限值对象）

禁止：

* `@Data`（因生成不可控的 equals/hashCode，且易导致字段暴露）
* `@SneakyThrows`
* `@AllArgsConstructor(access = PRIVATE)` 除外

---

## 2.7 注释规范（SHOULD）

注释应解释：

* 为什么这样设计（业务背景、权衡）
* 业务约束
* 外部兼容性
* 性能权衡

禁止无意义注释（如 `// 设置名称`）。

---

## 2.8 AI辅助注释（SHOULD）

复杂逻辑推荐：

```java
// AI:
// 订单取消后:
// 1. 回滚库存
// 2. 回滚优惠券
// 3. 发布取消事件
```

---

# 3. 领域建模规范

## 3.1 Domain层要求（MUST）

Domain层：

* 不依赖Spring
* 不依赖数据库
* 不依赖HTTP
* 不依赖MQ

必须纯业务。

---

## 3.2 贫血模型治理（SHOULD）

禁止：

```java
Order.setStatus()
```

推荐：

```java
order.pay()
order.cancel()
order.complete()
```

---

## 3.3 聚合根（MUST）

聚合根负责：

* 状态一致性
* 业务规则
* 生命周期

禁止绕过聚合直接修改子对象。

---

## 3.4 Domain Event（SHOULD）

领域事件命名：

```plaintext
OrderCreatedEvent
PaymentSucceededEvent
InventoryReservedEvent
```

事件必须：

* 不可变
* 可序列化
* 包含业务主键

---

# 4. 数据库规范

## 4.1 表设计（MUST）

所有业务主表（需要并发更新的表）必须包含：

```sql
id          BIGINT PRIMARY KEY,
created_at  DATETIME(3) NOT NULL,
updated_at  DATETIME(3) NOT NULL,
version     INT NOT NULL DEFAULT 0   -- 仅主表要求
```

推荐（所有表）：

```sql
created_by  VARCHAR(64),
updated_by  VARCHAR(64),
deleted_at  DATETIME(3)
```

---

## 4.2 字段规范（MUST）

* 禁止保留字
* 必须COMMENT
* 时间统一UTC
* 金额统一decimal
* 禁止float/double存金额

---

## 4.3 索引规范（MUST）

必须：

* 所有查询有索引
* 联合索引符合最左匹配
* 高频查询覆盖索引

禁止：

* 冗余索引
* 超长索引
* 低区分度单列索引

---

## 4.4 SQL规范（MUST）

禁止：

```sql
SELECT *
```

必须显式字段。

---

## 4.5 分页规范（MUST）

深度分页（偏移量 > 1000）禁止使用 OFFSET，必须使用游标方式：

```sql
WHERE id > last_id
```

普通分页（前几页）允许 OFFSET，但需确保查询有对应索引且性能满足要求。

---

## 4.6 ORM规范（MUST）

Entity：

* 只映射数据库
* 不承载业务逻辑

DTO与Entity禁止混用。

---

# 5. 接口规范

## 5.1 REST规范（MUST）

资源使用复数名词：

```plaintext
/orders
/users
/payments
```

---

## 5.2 HTTP语义（MUST）

| 方法     | 语义   |
| ------ | ---- |
| GET    | 查询   |
| POST   | 创建   |
| PUT    | 全量更新 |
| PATCH  | 部分更新 |
| DELETE | 删除   |

---

## 5.3 响应规范（MUST）

统一：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {}
}
```

但HTTP状态码必须正确。

禁止全部返回200。

---

## 5.4 错误码规范（MUST）

**【修订】** 错误码采用结构化格式，同时保证人类可读：

格式：`{DOMAIN}.{CODE}`

示例：
- `ORDER.1001` 表示订单未找到（配合message `ORDER_NOT_FOUND`）
- `PAYMENT.2003` 表示支付超时

具体约定：
- 前三位为领域缩写（ORDER, USER, PAYMENT等）
- 后四位为数字错误码（1000~1999 客户端错误，2000~2999 服务端错误，3000~3999 第三方错误）
- 同时必须提供可读的 message（如 `ORDER_NOT_FOUND`），便于日志与监控

响应示例：
```json
{
  "code": "ORDER.1001",
  "message": "ORDER_NOT_FOUND",
  "detail": "订单不存在"
}
```

禁止无结构字符串（如 `"1001"`、`"ERR001"`）。

---

## 5.5 OpenAPI（MUST）

所有接口必须：

* 自动生成OpenAPI
* 提供example
* 标记required
* 提供错误响应

禁止手写Markdown API文档。

---

## 5.6 幂等性（MUST）

所有**可能产生副作用且非天然幂等**的写接口（如创建、更新、取消）必须支持幂等。天然幂等的操作（如设置唯一键、追加日志）可豁免，但必须在接口文档中说明。

统一使用：

```plaintext
Idempotency-Key: <客户端生成的唯一键>
```

---

# 6. 事务与一致性

## 6.1 事务边界（MUST）

事务只能存在于：

```plaintext
application/service
```

禁止：

* controller事务
* repository事务嵌套

---

## 6.2 长事务治理（MUST）

事务内禁止：

* RPC
* MQ
* HTTP调用
* 大量循环

---

## 6.3 分布式事务（MUST）

优先级：

1. 本地事务
2. Outbox
3. 最终一致性
4. TCC

**【修订】** XA 和 2PC 默认禁止使用。如业务场景确实需要强一致性且其他方案无法满足（极少发生，如金融对账核心链路），必须：
- 提交架构评审 RFC
- 获得架构委员会特批
- 在代码中标记 `@DistributedTransaction(risk = "XA")` 并附带评审链接
- 同步所有下游风险

---

# 7. 稳定性治理

## 7.1 超时（MUST）

所有**跨进程调用**（RPC、HTTP、DB、MQ、缓存）必须设置超时。进程内调用可不用。

必须设置：

* 连接超时
* 读取超时
* 总超时

禁止无限等待。

---

## 7.2 重试（MUST）

仅幂等操作允许重试。

重试策略：

* 指数退避
* 最大3次
* 必须限流

---

## 7.3 熔断（MUST / SHOULD）

**【修订】** 
- **强依赖外部服务**（如支付网关、核心认证、库存中心）：必须支持熔断降级（MUST）
- **弱依赖外部服务**（如非核心数据上报、埋点、推送）：建议支持熔断，但允许仅做超时+重试（SHOULD）

熔断配置参考：错误率 > 50% 且最小请求数 ≥ 20 时触发，半开后成功比例 ≥ 80% 关闭。

---

## 7.4 缓存（MUST）

缓存必须：

* 设置TTL
* 防穿透
* 防击穿
* 防雪崩

---

# 8. 可观测性规范

## 8.1 日志（MUST）

**【修订】** 日志格式要求：
- **生产环境**：必须结构化 JSON（如 Logback JSON layout）
- **开发/测试环境**：允许使用文本格式（便于本地调试），但需确保与生产格式可互相转换（例如通过配置切换）

必须包含字段：

* traceId
* requestId
* userId（如有）

---

## 8.2 日志等级（MUST）

| 等级    | 场景     |
| ----- | ------ |
| ERROR | 需要人工介入 |
| WARN  | 可恢复异常  |
| INFO  | 核心业务流程 |
| DEBUG | 调试     |

---

## 8.3 禁止日志打印（MUST）

禁止打印：

* 密码
* Token
* 身份证
* 手机号原文

---

## 8.4 Metrics（MUST）

关键接口必须上报：

* QPS
* RT
* P95/P99
* Error Rate

---

## 8.5 Tracing（MUST）

所有 HTTP、RPC、MQ、异步线程必须传播 trace 上下文。

**【修订】** 具体实现要求：
- 使用 OpenTelemetry 自动注入（Javaagent 或 SDK）
- 对于自定义线程池，必须包装 `Executor`：
  ```java
  ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
  executor.setTaskDecorator(new WrappedTaskDecorator()); // 传递 trace 上下文
  ```
- `@Async` 方法需确保 `Executor` 已装饰，或使用 `TracingExecutor` 包装
- 使用 MDC 配合 `traceId` 的，需在异步任务开始时手动设置（推荐用 `Callable` 包装）

禁止出现 traceId 丢失导致链路断裂。

---

# 9. 安全规范

## 9.1 输入校验（MUST）

必须：

* 长度校验
* 格式校验
* 枚举校验
* 白名单校验

---

## 9.2 注入防御（MUST）

禁止：

* SQL拼接
* Shell拼接
* 动态路径拼接

---

## 9.3 权限控制（MUST）

必须：

* 鉴权
* 租户隔离
* 数据权限

---

## 9.4 密钥管理（MUST）

禁止明文：

* AK/SK
* Token
* 密码

统一使用：

* Vault
* K8s Secret
* Apollo Secret

---

# 10. 测试规范

## 10.1 测试分层（MUST）

| 类型               | 要求               |
| ---------------- | ---------------- |
| Unit Test        | 不依赖外部环境          |
| Integration Test | 使用Testcontainers |
| Contract Test    | 服务契约验证           |
| E2E              | 核心业务链路           |

---

## 10.2 覆盖率（MUST）

| 类型     | 覆盖率   |
| ------ | ------ |
| Domain | ≥ 80% |
| 新增代码   | ≥ 80% |

---

## 10.3 测试命名（MUST）

格式：

```plaintext
should_xxx_when_xxx
```

示例：

```plaintext
should_throwException_when_orderNotFound
```

---

# 11. CI/CD规范

## 11.1 CI门禁（MUST）

必须通过：

* 编译
* 单测
* 覆盖率
* Sonar扫描
* ArchUnit
* 安全扫描

---

## 11.2 发布规范（MUST）

必须支持：

* 灰度发布
* 金丝雀
* 回滚
* 优雅停机

---

## 11.3 优雅停机（MUST）

收到SIGTERM后：

1. 停止接流量
2. 等待请求完成
3. 关闭连接池
4. 提交缓冲数据

---

# 12. 配置治理

## 12.1 配置分层（MUST）

```plaintext
application.yml
application-dev.yml
application-test.yml
application-prod.yml
```

禁止多环境混用。

---

## 12.2 动态配置（SHOULD）

核心开关（如限流、熔断、灰度规则）必须动态生效；资源类配置（如连接池大小、线程池核心数）允许重启生效。

---

## 12.3 禁止硬编码配置（MUST）

所有可能因环境、业务策略或外部依赖而变化的值，必须外部化到配置文件（`application.yml` / Apollo / 环境变量），禁止以字面量硬编码在代码中。

### 必须外部化的配置类型

| 类型 | 示例 | 推荐承载 |
| --- | --- | --- |
| 网络与连接 | URL、host、port、超时、连接池大小 | application.yml + Apollo |
| 凭据与密钥 | AK/SK、密码、token、JWT secret | Vault / K8s Secret / Apollo Secret（参见 9.4） |
| 第三方服务地址 | 支付网关、SSO IdP、SAML metadata、邮件/短信服务 | Apollo |
| 业务策略阈值 | 锁定次数、锁定时长、密码长度、token 有效期、限流阈值、重试次数、熔断比例 | Apollo（动态生效） |
| 业务身份标识 | tenantId、appId、entityId、issuer、租户白名单 | application.yml + Apollo |
| 文件路径与目录 | 上传路径、日志目录、模板路径 | application.yml |
| 功能开关 | feature flag、灰度开关、降级开关 | Apollo（动态生效） |
| 文案与提示 | 邮件模板、错误提示文案、国际化文本 | i18n properties / 模板文件 |

### 命名与绑定规范

* 必须使用 `@ConfigurationProperties` 类型安全绑定，禁止散落使用 `@Value`：
  ```java
  @ConfigurationProperties(prefix = "auth.lockout")
  public record LockoutProperties(int maxFailedAttempts, Duration lockDuration) {}
  ```
* 配置 key 必须使用业务前缀（如 `auth.jwt.*`、`auth.saml.*`），禁止顶层散落 key。
* 配置类必须 `@Validated`，对必填项使用 `@NotNull` / `@NotBlank` / `@Min`。
* 时间类型使用 `java.time.Duration`（如 `30m`、`PT30M`），禁止直接用 `int 秒数` / `long 毫秒数` 表达时间语义。

### 允许硬编码的例外

仅以下情况允许字面量出现在代码中：
* **领域常量**：业务规则一部分且永不随环境变化（如 `BCRYPT_COST = 10` 中的 cost 因子若已是密码学规范），且必须以 `static final` 命名常量声明在 `shared/constants` 下，附 Javadoc 注明来源。
* **协议或规范固定值**：如 HTTP 状态码、`application/json`、SAML NameID format URI、JWT 算法名 `HS256`。
* **测试代码**：单元测试中的固定输入。
* **错误码与枚举**：错误码字符串、状态枚举值（其本身就是契约的一部分）。

凡是上述例外以外的字面量，CR/PR 时必须被驳回。

### 自动化检查

* CI 必须运行 ArchUnit / 自定义 Checkstyle 规则，禁止以下模式直接出现在 `domain` / `application` / `interfaces` / `infrastructure` 层（除常量类与测试代码）：
  * 形如 `http://`、`https://`、`jdbc:`、`smtp://`、`amqp://` 的 URL 字面量
  * IP 地址正则 `\d+\.\d+\.\d+\.\d+`
  * 端口字面量后跟数字（`new Socket(host, 6379)`）
  * 超时字面量（`Duration.ofSeconds(N)` 中的 N、`setTimeout(N)`）当 N 出现在业务代码而非配置绑定方法时
* 违反必须在 PR 中修复或申请 RFC 例外。

### 反例与正例

反例：
```java
// 禁止：硬编码业务策略
if (user.getFailedAttempts() >= 5) {
    user.lockUntil(LocalDateTime.now().plusMinutes(30));
}

// 禁止：硬编码外部地址
RestTemplate rt = new RestTemplate();
rt.getForObject("http://payment.internal:8080/charge", ...);
```

正例：
```java
@RequiredArgsConstructor
public class LockoutDomainService {
    private final LockoutProperties props;

    public void applyLockout(InternalUser user) {
        if (user.getFailedAttempts() >= props.maxFailedAttempts()) {
            user.lockUntil(LocalDateTime.now().plus(props.lockDuration()));
        }
    }
}
```

---

## 12.4 配置审计（MUST）

必须记录：

* 操作人
* 时间
* 内容
* 回滚记录

---

# 13. 技术债治理

## 13.1 TODO规范（MUST）

所有TODO必须：

* 关联Issue
* 指定负责人
* 指定截止版本

---

## 13.2 依赖治理（MUST）

每月：

* 检查CVE
* 检查过期依赖
* 检查许可证风险

---

## 13.3 架构评审（MUST）

以下变更必须评审：

* 新中间件
* 新架构模式
* 新数据库
* 新通信协议

---

# 14. AI协同开发附录

## 14.1 AI可读工程要求（MUST）

项目根目录必须包含：

```plaintext
/docs
    glossary.md
    architecture.md
    module-map.md
    coding-rules.md
```

---

## 14.2 glossary.md（MUST）

必须定义：

* 核心业务术语
* 状态机含义
* 缩写解释
* 边界定义

---

## 14.3 module-map.md（MUST）

必须说明：

* 模块职责
* 模块边界
* 数据归属
* 对外接口

---

## 14.4 AI禁止事项（MUST）

AI 可生成草案，但以下内容必须经过人工 Review，Review人承担最终责任：

* 数据库迁移脚本（Flyway/Liquibase）
* 分布式事务逻辑
* 安全敏感代码（权限、加密、密钥处理）
* 核心定价/计费逻辑

未经人工 Review 不得直接上线。

---

## 14.5 AI生成代码要求（MUST）

AI生成代码必须：

* 通过静态扫描
* 补充测试
* 补充注释
* 补充OpenAPI
* 符合目录结构

---

# 15. 规范执行机制

## 15.1 自动化治理（MUST）

规范必须通过工具自动校验：

| 类型   | 工具                     |
| ---- | ---------------------- |
| 分层依赖 | ArchUnit               |
| 代码规范 | Checkstyle / Spotless  |
| 静态扫描 | SonarQube              |
| 安全扫描 | OWASP Dependency Check |
| 格式化  | Spotless / Prettier    |

要求使用 **ArchUnit** 编写自定义规则（如分层依赖、继承限制、命名约定），并在 CI 中运行。

---

## 15.2 RFC机制（MUST）

以下变更必须RFC：

* 架构调整
* 技术栈升级
* 规范例外
* 中间件引入

---

## 15.3 规范版本治理（MUST）

规范变更必须：

* 版本号升级
* 更新脚手架
* 更新CI规则
* 更新模板工程

---

# 附录：推荐技术栈（Spring生态）

| 分类         | 推荐                      |
| ---------- | ----------------------- |
| Framework  | Spring Boot             |
| Cloud      | Spring Cloud            |
| ORM        | JPA/Hibernate 或 MyBatis |
| Mapping    | MapStruct               |
| Validation | Hibernate Validator     |
| Metrics    | Micrometer + Prometheus |
| Logging    | Logback JSON            |
| Trace      | OpenTelemetry           |
| Test       | JUnit5 + Testcontainers |
| Build      | Maven/Gradle            |
| CI         | GitHub Actions/Jenkins  |

---

# 附录：禁止清单（MUST）

禁止：

* 巨型Service（>2000行）
* God Object
* 循环依赖
* 超级Common模块
* Map万能传参
* Util到处横飞
* 魔法字符串
* 魔法数字
* SELECT *
* 无索引查询
* 全局异常吞没
* ThreadLocal业务上下文滥用
* BeanUtils复制
* Entity直接返回前端
* Controller写业务逻辑
* Repository写业务规则
* 业务策略阈值/外部地址/凭据/超时硬编码（必须外部化到配置文件，参见 12.3）

---

**规范版本**：v1.0
**规范状态**：正式版  
**适用范围**：所有后端服务项目  
**所有者**：架构委员会  
**生效日期**：2026-06-01  
---