# EcoSaaS 采购平台

企业级采购管理系统，覆盖从供应商管理、询报价、合同管理到付款的完整采购流程。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Java 21 + Spring Boot 3.5 + Spring Security |
| 数据库 | PostgreSQL 16 + Spring Data JPA + Druid 连接池 |
| 认证 | JWT (JJWT 0.12.6) + SAML 2.0 SSO |
| 配置中心 | Apollo |
| 前端框架 | Vue 3 + TypeScript + Vite 6 |
| UI 组件库 | Ant Design Vue 4 |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |
| API 文档 | SpringDoc OpenAPI |

## 项目结构

```
├── backend/                          # 后端 Spring Boot 应用
│   └── src/main/java/com/cdp/ecosaas/procurement/
│       ├── shared/                   # 跨模块共享代码
│       │   ├── exception/            # 全局异常处理 & 业务异常基类
│       │   ├── model/                # 通用模型（PageQuery, PageResult）
│       │   └── util/                 # 工具类（SecurityUtils）
│       ├── auth/                     # 认证与权限模块（DDD 分层，已完成）
│       │   ├── domain/               # 领域层（模型、服务、仓储接口、端口）
│       │   ├── application/          # 应用层（命令、查询、处理器）
│       │   ├── infrastructure/       # 基础设施层（JPA、安全、外部服务）
│       │   ├── interfaces/           # 接口层（REST Controller、DTO）
│       │   └── shared/               # 模块内共享（常量、异常）
│       ├── supplier/                 # 供应商管理模块（骨架）
│       ├── pr/                       # 采购申请单模块（骨架）
│       ├── rfq/                      # 询报价管理模块（骨架）
│       ├── contract/                 # 合同管理模块（骨架）
│       ├── order/                    # 采购订单 (PO) 模块（骨架）
│       ├── payment/                  # 付款管理模块（骨架）
│       ├── report/                   # 报表管理模块（骨架）
│       └── setting/                  # 系统设置模块（骨架）
│
│       # 上述业务模块均沿用 auth 的 DDD 分层（每个模块统一为以下 19 个叶子包）：
│       #   domain/{model,service,repository,port,event}
│       #   application/{command,query,handler,service}
│       #   infrastructure/{config,external,persistence/{entity,mapper,repository}}
│       #   interfaces/{rest,dto}
│       #   shared/{constants,enums,exception}
│       # 每个叶子包含一个 package-info.java 占位并说明该包职责。
│
├── frontend/                         # 前端 Vue 3 应用
│   └── src/
│       ├── shared/                   # 跨模块共享代码
│       │   ├── http/                 # Axios 实例 & CSRF 适配器
│       │   ├── types/                # 通用类型（分页）
│       │   └── utils/                # 工具函数（错误处理）
│       ├── modules/
│       │   └── auth/                 # 认证模块（DDD 分层）
│       │       ├── domain/           # 领域层（实体、值对象、规则）
│       │       ├── application/      # 用例层
│       │       ├── infrastructure/   # 服务层（API 调用）
│       │       ├── presentation/     # 展示层（视图、组件、路由、Store）
│       │       └── types/            # 类型定义（DTO、VO、Command）
│       ├── router/                   # 全局路由
│       └── views/                    # 全局页面
│
└── init-docs/                        # 需求与设计文档
```

## 模块规划

| # | 模块 | 后端包名 | 状态 | 说明 |
|---|------|---------|------|------|
| 01 | 认证与权限管理 | `auth` | ✅ 已完成 | 登录、SSO、密码管理、角色权限、审计日志 |
| 02 | 供应商管理 | `supplier` | � 骨架就绪 | 供应商注册、信息维护、证件管理、审核 |
| 03 | 采购申请 (PR) | `pr` | � 骨架就绪 | PR 发起、审批流、分配采购员 |
| 04 | 询报价 (RFQ) | `rfq` | � 骨架就绪 | 询价单、供应商报价、核价 |
| 05 | 合同管理 | `contract` | � 骨架就绪 | 合同创建、审批、签署、归档 |
| 06 | 采购订单与付款 | `order` / `payment` | � 骨架就绪 | PO 生成、收货、对账、付款（拆为两个包） |
| 07 | 报表与系统设置 | `report` / `setting` | � 骨架就绪 | 数据报表、字典管理、系统配置（拆为两个包） |

> 状态说明：✅ 已完成（功能可用）｜📦 骨架就绪（已建 DDD 分层包与 `package-info.java`，待填充实现）｜🔲 待开发。

**总体页面结构：**

- `src/layouts/MainLayout.vue` — 主布局，包含侧边栏菜单、顶部栏（用户信息/退出）、内容区
- `src/config/menu.ts` — 按角色配置的菜单数据，4 种角色各有独立菜单
- `src/router/index.ts` — 完整路由表，所有模块的页面路由已预注册
- `src/views/placeholder/PlaceholderView.vue` — 占位页面，未实现的模块显示"待开发"提示

**菜单结构（按角色）：**

| 采购经理 (ADMIN) | 采购员 (BUYER) | 业务人员 | 供应商 |
|---|---|---|---|
| 经理工作台 | 工作台 | 我的采购申请 | 企业信息 |
| 审批中心 | 供应商管理 | 创建采购申请 | 联系人管理 |
| 采购申请单 | 审批中心 | 我的合同 | 我的询价单 |
| 询价单查询 | 采购申请单 | 我的PO | 合同管理 |
| 合同管理 | 询价单管理 | | |
| 履约与付款 | 合同管理 | | |
| 采购分析 | 履约与付款 | | |
| 系统管理 ▸ | 邮件日志 | | |
| 邮件日志 | | | |

系统管理子菜单：账号管理（已实现）、审计日志（已实现）、字段设置、表单设置。

## 快速开始

### 环境要求

- Java 21+
- Node.js 18+
- PostgreSQL 16+
- Maven 3.9+

### 后端启动

```bash
cd backend

# 1. 创建数据库
psql -U postgres -c "CREATE DATABASE trial_procurement ENCODING 'UTF8';"

# 2. 执行迁移脚本
psql -U postgres -d trial_procurement -f src/main/resources/db/migration/V1__init_auth_tables.sql
psql -U postgres -d trial_procurement -f src/main/resources/db/migration/V2__init_super_admin.sql

# 3. 配置数据库连接（修改 application.yml 或使用 Apollo）

# 4. 编译运行
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Denv=DEV"
```

后端默认运行在 `http://localhost:9000`。

#### IDE 启动（VS Code / Kiro）

项目已配置 `backend/.vscode/launch.json`，支持以下启动方式：

| 配置名称 | 用途 | JVM 参数 |
|----------|------|----------|
| Run ProcurementApplication | 正常运行 | `-Denv=DEV` |
| Debug ProcurementApplication | 调试模式 | `-Denv=DEV -Xdebug` |
| Current File | 运行当前文件 | — |

在 VS Code / Kiro 中按 `F5` 或从 Run 面板选择配置即可启动。

#### 命令行启动

```bash
cd backend

# Run 模式
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Denv=DEV"

# Debug 模式（远程调试端口 5005）
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Denv=DEV -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# 或直接用 java 命令（需先 mvn package）
mvn clean package -DskipTests
java -Denv=DEV -jar target/ecosaas-procurement-0.0.1.snapshot.jar

# Debug 模式
java -Denv=DEV -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/ecosaas-procurement-0.0.1.snapshot.jar
```

`-Denv=DEV` 参数用于 Apollo 配置中心识别环境。

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`，API 请求自动代理到后端 9000 端口。

### 默认管理员账号

| 字段 | 值 |
|------|------|
| 手机号 | 13800000000 |
| 密码 | Admin@123456 |
| 角色 | 超级管理员 |

## 架构设计

### 后端 DDD 分层

```
interfaces/     → REST API、DTO 转换
application/    → 命令处理、查询处理、应用服务
domain/         → 聚合根、领域服务、仓储接口、端口接口
infrastructure/ → JPA 实现、安全组件、外部服务适配器
```

### 认证机制

- JWT Token 通过 `HttpOnly` + `Secure` + `SameSite=Lax` Cookie 传输
- CSRF 防御采用 Double Submit Cookie 模式
- 支持 Worklife SSO (SAML 2.0) + 手机号密码双通道登录
- 会话超时 30 分钟，支持滑动过期

### 角色体系

| 角色 | 说明 |
|------|------|
| ADMIN | 管理员/采购经理，拥有全量数据权限 |
| BUYER | 采购员，管理分配的供应商和询报价 |
| BUSINESS_USER | 业务人员，发起 PR、查看进度 |
| SUPPLIER | 供应商，管理企业信息和报价 |

## 开发规范

### 新增模块

> 模块 02–07 的后端包骨架已按 DDD 分层创建（见上方「模块规划」），开发时直接在对应包内填充实现即可，无需重建目录。

1. 后端在 `com.cdp.ecosaas.procurement.{module}` 下按 DDD 分层创建包
2. 业务异常继承 `shared.exception.BusinessException`
3. 分页查询使用 `shared.model.PageQuery` / `PageResult`
4. 获取当前用户使用 `shared.util.SecurityUtils`
5. 前端在 `src/modules/{module}` 下按 domain/application/infrastructure/presentation 分层

### 运行测试

```bash
cd backend
mvn test
```

## API 文档

启动后端后访问：`http://localhost:9000/swagger-ui/index.html`

## License

Proprietary - CDP EcoSaaS
