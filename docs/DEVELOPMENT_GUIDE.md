# 开发指南

## 1. 环境要求

| 工具 | 版本 |
|------|------|
| Android Studio | Ladybug (2024.2) 或更高 |
| JDK | 17 |
| Android SDK | minSdk 26, targetSdk 35 |
| Gradle | 8.x (KTS) |
| Kotlin | 2.0+ |

## 2. 项目初始化

```bash
# 1. 在 Android Studio 中创建新项目
#    模板: Empty Compose Activity
#    包名: com.example.fangbianjizhang
#    语言: Kotlin
#    Minimum SDK: API 26

# 2. 克隆到本地后安装依赖
git clone https://github.com/{owner}/fangbianjizhang.git
cd fangbianjizhang
./gradlew build
```

## 3. 核心依赖

在 `build.gradle.kts (app)` 中添加：

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.fangbianjizhang"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // 图表库 (Vico)
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
}
```

## 4. 编码规范

### 4.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 全小写 | `com.example.fangbianjizhang.ui.home` |
| 类名 | PascalCase | `HomeViewModel`, `AccountEntity` |
| 函数名 | camelCase | `getByType()`, `insertTransaction()` |
| 常量 | UPPER_SNAKE | `MAX_NOTE_LENGTH`, `DEFAULT_CURRENCY` |
| 数据库列 | snake_case | `account_id`, `total_limit` |
| Composable | PascalCase | `HomeScreen()`, `BudgetCard()` |

### 4.2 金额处理规则

```
禁止：Double / Float 参与任何金额计算
必须：所有金额以 Long（分）存储和计算
转换：仅在 UI 输入/显示时进行 元 <-> 分 转换
工具：统一使用 AmountFormatter.toLong() / toDisplay()
```

### 4.3 文件规模

- 单文件不超过 400 行（上限 800 行）
- 单函数不超过 50 行
- ViewModel 过大时拆分为多个 UseCase

## 5. 开发流程

### 5.1 分支策略

```
main          ← 稳定发布分支
├── develop   ← 开发主分支
│   ├── feature/home-page
│   ├── feature/statistics
│   └── fix/budget-calculation
```

- `main`: 仅合并经过测试的 develop
- `develop`: 日常开发，feature 分支合入
- `feature/*`: 功能开发分支
- `fix/*`: Bug 修复分支

### 5.2 提交规范

```
feat: 添加月度统计饼图
fix: 修复信用账户余额计算错误
refactor: 重构交易插入逻辑
docs: 更新数据库设计文档
test: 添加预算计算单元测试
chore: 升级 Room 版本到 2.6.1
```

### 5.3 开发顺序建议

按依赖关系推荐的开发顺序：

1. **基础层**：Room 数据库 + Entity + DAO + Migration
2. **数据层**：Repository 实现 + Mapper
3. **领域层**：Domain Model + UseCase
4. **DI 层**：Hilt Module 配置
5. **UI 基础**：Theme + Navigation + 底部导航栏
6. **首页**：流水列表 + 日分组 + 预算卡片
7. **记账页**：四种类型表单 + 计算器键盘（四则运算）
8. **资产页**：四类账户展示 + 新增/编辑
9. **统计页**：月度统计 + 饼图 + 分类排行
10. **设置页**：分类管理 + 预算设置
11. **定期扣款**：模板管理 + WorkManager
12. **导入导出**：JSON 备份 + CSV 导出
13. **热更新**：GitHub Releases 检查

## 6. 发布流程

### 6.1 签名配置

```kotlin
// build.gradle.kts (app)
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

签名密钥通过环境变量注入，禁止提交到代码仓库。

### 6.2 构建发布 APK

```bash
# 构建 Release APK
./gradlew assembleRelease

# 输出路径
# app/build/outputs/apk/release/app-release.apk
```

### 6.3 GitHub Release 发布

```bash
# 打 tag
git tag -a v1.0.0 -m "v1.0.0: 核心记账功能"
git push origin v1.0.0

# 在 GitHub 创建 Release
# 1. 上传 app-release.apk 作为 Asset
# 2. 填写更新日志（Markdown 格式）
# 3. 发布
```

## 7. 测试策略

### 7.1 单元测试

重点覆盖：
- `AmountFormatter`：分/元转换精度
- 各 UseCase：业务逻辑正确性
- 账户余额更新：各类型账户的加减逻辑
- 预算计算：TOTAL 和 PER_CATEGORY 两种模式

```kotlin
// 示例
@Test
fun `credit account expense increases used amount`() {
    val account = Account(type = CREDIT, totalLimit = 5000_00, usedAmount = 1000_00)
    val result = account.applyExpense(200_00)
    assertEquals(1200_00, result.usedAmount)
}
```

### 7.2 数据库测试

使用 Room 的 `MigrationTestHelper` 测试迁移：

```kotlin
@Test
fun migrate1To2() {
    helper.createDatabase(DB_NAME, 1).close()
    val db = helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)
    // 验证新字段存在且默认值正确
}
```

### 7.3 常见问题排查

| 问题 | 排查方向 |
|------|----------|
| 余额不对 | 检查 @Transaction 是否包含所有更新操作 |
| 饼图数据错误 | 检查 SQL 是否正确聚合到一级分类 |
| 定期任务未执行 | 检查 WorkManager 约束条件和 next_due_date |
| 导入后数据丢失 | 检查 schema_version 兼容性和冲突策略 |
| 信用额度显示错误 | 确认是 total_limit - used_amount 而非相反 |
