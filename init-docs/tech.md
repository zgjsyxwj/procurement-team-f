
## 后端技术栈版本清单

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| JDK | Java | 21 | LTS 版本 |
| 开发框架 | Spring Boot | 3.5.4 | 核心框架 |
| 开发框架 | Spring Cloud | 2025.0.0 | 微服务框架 |
| 安全框架 | Spring Security | (Boot 管理) | 认证与授权 |
| 数据库 | PostgreSQL JDBC Driver | 42.7.5 | 数据库驱动 |
| 连接池 | Alibaba Druid | 1.2.27 | 数据库连接池 |
| ORM | Spring Data JPA (Hibernate) | (Boot 管理) | 对象关系映射 |
| 远程调用 | Spring Cloud OpenFeign | (Cloud 管理) | 声明式 HTTP 客户端 |
| 应用监控 | Spring Boot Actuator | (Boot 管理) | 应用健康与指标 |
| 应用监控 | Micrometer Prometheus | (Boot 管理) | Prometheus 指标导出 |
| 配置中心 | Apollo Client | 2.4.0 | 携程分布式配置中心 |
| POJO 简化 | Lombok | 1.18.38 | 减少样板代码 |
| POJO 简化 | MapStruct | 1.6.3 | Bean 映射转换 |
| POJO 简化 | Lombok-MapStruct Binding | 0.2.0 | Lombok 与 MapStruct 协同 |
| 序列化 | Jackson (Spring Boot 默认) | (Boot 管理) | JSON 序列化（禁用 Fastjson，历史安全漏洞风险） |
| API 文档 | SpringDoc OpenAPI | 2.8.9 | OpenAPI 3 文档生成 |
| 登录凭证 | JJWT | 0.12.6 | JWT 令牌（jjwt-api + jjwt-impl + jjwt-jackson） |
| 工具库 | Apache Commons Lang3 | 3.18.0 | 通用工具类 |
| 工具库 | Apache Commons Collections4 | 4.5.0 | 集合工具类 |
| 日志 | Logback | (Boot 管理) | 日志实现 |
| 测试 | Spring Boot Test (JUnit 5) | (Boot 管理) | 单元/集成测试 |
| 测试 | Mockito | 5.19.0 | Mock 框架 |
| 编译工具 | Maven | - | 项目构建 |
| 编译插件 | maven-compiler-plugin | 3.14.0 | Java 编译 |
| 编译插件 | maven-surefire-plugin | 3.1.2 | 测试执行 |
| 打包插件 | spring-boot-maven-plugin | 3.5.4 | Fat JAR 打包 |

## 前端技术栈版本清单

### 运行环境

| 工具 | 版本 | 说明 |
|------|------|------|
| Node.js | 22.x LTS | 长期支持版本 |
| pnpm | 10.x | 包管理器 |
| Git | 2.x | 版本控制 |

### 核心框架

| 库 | 版本 | 说明 |
|------|------|------|
| Vue | ^3.5 | 核心框架（Composition API + `<script setup>`） |
| Vite | ^7.x | 构建工具 |
| TypeScript | ^5.9 | 类型系统 |
| Vue Router | ^4.5 | 路由管理 |
| Pinia | ^3.0 | 状态管理（Vue 3 专用版） |

### UI 与样式

| 库 | 版本 | 说明 |
|------|------|------|
| ant-design-vue | ^4.2 | UI 组件库 |
| @ant-design/icons-vue | ^7.0 | 图标库 |
| Tailwind CSS | ^4.3 | 原子化 CSS（CSS-first 配置） |
| @tailwindcss/vite | ^4.3 | Tailwind Vite 插件 |
| class-variance-authority | ^0.7 | 组件变体样式 |
| clsx | ^2.1 | 条件类名 |
| tailwind-merge | ^3.2 | Tailwind 类名合并 |
| lucide-vue-next | latest | 图标库（替代部分自定义图标） |

### 业务工具库

| 库 | 版本 | 说明 |
|------|------|------|
| @vueuse/core | ^13.x | Vue 组合式工具集 |
| vue-i18n | ^11.x | 国际化 |
| axios | ^1.7 | HTTP 请求 |
| dayjs | ^1.11 | 日期处理 |
| echarts | ^5.6 | 图表 |
| lodash-es | ^4.17 | 工具函数（ESM 版本） |
| mitt | ^3.0 | 事件总线 |
| uuid | ^10.0 | UUID 生成 |
| jsencrypt | ^3.3 | RSA 加密 |
| swiper | ^11.x | 轮播组件 |
| @lottiefiles/dotlottie-vue | latest | Lottie 动画 |

### 开发工具

| 库 | 版本 | 说明 |
|------|------|------|
| ESLint | ^9.x | 代码检查（Flat Config） |
| @antfu/eslint-config | latest | ESLint 预设配置 |
| Vitest | ^3.2 | 单元测试 |
| @vue/test-utils | latest | Vue 组件测试工具 |
| unplugin-auto-import | latest | API 自动导入 |
| unplugin-vue-components | latest | 组件自动注册 |
| antdv-component-resolver | latest | antdv 组件解析器 |
| @vitejs/plugin-basic-ssl | latest | HTTPS 本地开发 |
| @intlify/unplugin-vue-i18n | latest | i18n Vite 插件 |
| husky | ^9.x | Git Hooks |
| lint-staged | ^15.x | 暂存区 lint |
| less | ^4.2 | CSS 预处理器（antdv 主题定制用） |
| cross-env | ^7.0 | 跨平台环境变量 |