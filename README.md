# 方便记账

一款 Android 本地记账应用，支持多账户管理、预算控制、分类统计、定期扣款和数据备份。

## 功能亮点

- **四大账户类型**：资金账户、信用账户、投资账户、贷款账户
- **灵活记账**：支持收入、支出、转账、借贷四种方式
- **预算管理**：总额预算或分类预算，二选一
- **统计分析**：月度/年度统计、分类饼图、自定义日期范围
- **定期扣款**：信用卡还款、房贷月供、定投自动执行
- **数据备份**：JSON 全量备份 + CSV 流水导出，一键换机
- **版本更新**：GitHub Releases 热更新

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| 数据库 | Room (SQLite) |
| 依赖注入 | Hilt |
| 异步 | Coroutines + Flow |
| 后台任务 | WorkManager |
| 偏好存储 | DataStore |

## 文档索引

| 文档 | 说明 |
|------|------|
| [产品需求文档](docs/PRD.md) | 功能模块详细需求 |
| [数据库设计](docs/DATABASE.md) | 表结构、索引、SQL 模板 |
| [技术架构设计](docs/TECH_DESIGN.md) | 分层架构、包结构、数据流 |
| [UI/UX 规范](docs/UI_DESIGN.md) | 页面布局、交互流程、主题配色 |
| [内部接口规范](docs/API_SPEC.md) | Repository、UseCase、ViewModel 定义 |
| [开发指南](docs/DEVELOPMENT_GUIDE.md) | 环境搭建、编码规范、发布流程 |

## 版本规划

| 版本 | 内容 |
|------|------|
| v1.0 | 核心记账 + 账户管理 + 分类统计 |
| v1.1 | 预算管理 + 定期扣款 + 分类自定义 |
| v1.2 | 导入导出 + GitHub 热更新 |
| v1.3 | UI 美化 + 首页中文化 + 明细编辑/删除 + 外部数据导入修复 |
| v1.4 | 记账键盘适配 + 明细全字段编辑 + 资产余额修改 + 饼图标注旋转 + 热更新代理 |
| v1.4.1 | 修复分类预算模式下首页预算显示为0 |

## 项目结构

```
com.example.fangbianjizhang/
├── di/                  # Hilt 依赖注入模块
├── data/                # 数据层
│   ├── local/db/        # Room 数据库、DAO、Entity
│   ├── local/datastore/ # DataStore 偏好
│   ├── repository/      # Repository 实现
│   └── mapper/          # Entity ↔ Domain 映射
├── domain/              # 领域层
│   ├── model/           # 领域模型
│   ├── repository/      # Repository 接口
│   └── usecase/         # 用例（按功能分组）
├── ui/                  # 表现层
│   ├── theme/           # Material 3 主题
│   ├── navigation/      # 导航
│   ├── home/            # 首页
│   ├── asset/           # 资产
│   ├── statistics/      # 统计
│   ├── settings/        # 设置
│   └── record/          # 记账
├── worker/              # WorkManager 后台任务
└── util/                # 工具类
```

## 环境要求

- Android Studio Ladybug 或更高版本
- JDK 17
- Android SDK: minSdk 26 (Android 8.0), targetSdk 35
- Gradle 8.x (KTS)
