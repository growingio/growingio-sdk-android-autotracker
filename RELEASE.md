# 发布新版本指南

本文档介绍如何发布 GrowingIO Android Autotracker SDK 的新版本。

## 版本发布流程

### 1. 准备工作

在发布新版本之前，请确保：

- [ ] 所有计划的功能已完成并合并到 `master` 分支
- [ ] 所有测试通过
- [ ] 代码已通过 Code Review
- [ ] 更新了相关文档

### 2. 更新版本号

编辑 `gradle/libs.versions.toml` 文件，更新版本号：

```toml
[versions]
growingio = "x.y.z"  # 更新为新版本号
growingioCode = "xxyyzz"  # 更新版本代码
```

版本号规则：
- **主版本号(x)**: 重大更新，可能包含不兼容的 API 变更
- **次版本号(y)**: 新功能添加，向后兼容
- **修订号(z)**: Bug 修复，向后兼容

版本代码计算方式：将版本号 `x.y.z` 转换为 `xxyyzz` 格式，例如 `4.5.3` -> `40503`

### 3. 提交版本更新

```bash
git add gradle/libs.versions.toml
git commit -m "chore: bump version to x.y.z"
git push origin master
```

### 4. 创建 GitHub Release

#### 方式一：通过 GitHub Web 界面

1. 访问 [Releases 页面](https://github.com/growingio/growingio-sdk-android-autotracker/releases)
2. 点击 "Draft a new release"
3. 填写以下信息：
   - **Tag version**: `vx.y.z`（例如 `v4.5.3`）
   - **Target**: `master` 分支
   - **Release title**: `vx.y.z` 或更具描述性的标题
   - **Description**: 详细描述本次发布的更新内容
     ```markdown
     ## What's Changed
     
     ### 新特性
     - 添加的新功能说明
     
     ### Bug 修复
     - 修复的问题说明
     
     ### 改进
     - 性能优化或其他改进
     
     ## 完整更新日志
     查看 [所有提交](https://github.com/growingio/growingio-sdk-android-autotracker/compare/vx.y.z-1...vx.y.z)
     ```
4. 点击 "Publish release"

#### 方式二：使用 GitHub CLI

```bash
gh release create vx.y.z \
  --title "vx.y.z" \
  --notes "发布说明内容" \
  --target master
```

### 5. 自动发布到 Maven Central

创建 GitHub Release 后，会自动触发 `.github/workflows/publish.yml` 工作流：

1. 工作流会自动构建所有模块
2. 对构建产物进行签名
3. 发布到 Maven Central

可以在 [Actions 页面](https://github.com/growingio/growingio-sdk-android-autotracker/actions) 查看发布进度。

### 6. 验证发布

发布完成后（通常需要几小时同步到 Maven Central）：

1. 访问 [Maven Central Repository](https://central.sonatype.com/search?q=com.growingio.android)
2. 搜索 `com.growingio.android` 确认新版本已发布
3. 在测试项目中更新依赖版本并验证功能

## 高级发布选项

### 手动发布特定模块

如果只需要发布单个模块（例如修复了某个模块的问题），可以使用手动工作流：

1. 访问 [Actions 页面](https://github.com/growingio/growingio-sdk-android-autotracker/actions)
2. 选择 "Publish SDK Module manual" 工作流
3. 点击 "Run workflow"
4. 选择要发布的模块和版本号
5. 点击 "Run workflow" 确认

可发布的模块包括：
- `autotracker-bom`
- `growingio-hybrid`
- `growingio-ads`
- `growingio-abtest`
- `growingio-apm`
- `growingio-flutter`
- `growingio-compose`
- 以及其他 SDK 核心模块

### 发布测试版本（Beta）

如果需要在非 `master` 分支发布测试版本：

1. 确保当前在要测试的分支上
2. 访问 [Actions 页面](https://github.com/growingio/growingio-sdk-android-autotracker/actions)
3. 选择 "Publish Maven Manual" 工作流
4. 点击 "Run workflow"
5. 选择要运行的分支
6. 点击 "Run workflow" 确认

**注意**: 手动发布仅允许在非 `master` 分支上运行，用于测试目的。

### 本地测试发布

在实际发布到 Maven Central 之前，可以先在本地测试：

```bash
# 构建并发布到本地 Maven 仓库
bash ./gradle/publishAllToMavenLocal.sh

# 或者单独发布某个模块
./gradlew :module-name:publishToMavenLocal
```

发布到本地后，可以在测试项目中通过 `mavenLocal()` 仓库引用：

```groovy
repositories {
    mavenLocal()
    // ... 其他仓库
}
```

## 发布检查清单

在发布新版本前，请确认：

- [ ] 版本号已在 `gradle/libs.versions.toml` 中更新
- [ ] 所有测试通过 (运行 `./gradlew test`)
- [ ] 代码格式检查通过 (运行 `./gradlew spotlessCheck`)
- [ ] 已更新相关文档
- [ ] 已在本地或测试环境验证新功能
- [ ] 已准备好 Release Notes
- [ ] GitHub Release 已创建
- [ ] 自动发布工作流运行成功
- [ ] 新版本已在 Maven Central 上可用

## 问题排查

### 发布工作流失败

1. 检查 [Actions 日志](https://github.com/growingio/growingio-sdk-android-autotracker/actions) 获取详细错误信息
2. 常见问题：
   - **签名失败**: 检查 Secrets 配置是否正确
   - **构建失败**: 本地运行 `./gradlew build` 检查是否有编译错误
   - **发布失败**: 检查 Maven Central 凭据是否有效

### 版本已存在错误

如果遇到版本已存在的错误：
- Maven Central 不允许覆盖已发布的版本
- 需要增加版本号重新发布

### 构建缓存问题

如果遇到缓存导致的构建问题：
```bash
./gradlew clean
./gradlew build --no-build-cache
```

## 相关资源

- [Maven Central Repository](https://central.sonatype.com/)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Gradle Maven Publish Plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
- [SDK 集成文档](https://growingio.github.io/growingio-sdk-docs/)

## 联系方式

如有问题，请联系：
- Email: sdk-integration@growingio.com
- 提交 [GitHub Issue](https://github.com/growingio/growingio-sdk-android-autotracker/issues)
