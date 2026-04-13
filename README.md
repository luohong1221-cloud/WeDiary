# Diary

一个基于 `Kotlin + Jetpack Compose + Room + MVVM` 的 Android 离线日记应用。

项目已经具备完整的本地记录主流程，重点覆盖时间线浏览、多图日记、标签组织、日历查看与全文搜索，适合作为 Android 作品集项目、日记类 App 原型或二次开发基础。

## 功能亮点

- 本地离线存储，日记内容默认保存在设备本地
- 首页时间线展示，支持收藏和删除
- 支持创建和编辑日记
- 支持标题、正文、心情、天气、标签
- 支持多图选择、私有目录存储、缩略图生成
- 支持日历视图查看每日记录
- 支持基于 Room FTS 的全文搜索
- 支持标签管理
- 支持主题、字体大小、自动保存等本地设置

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room + KSP
- DataStore Preferences
- Coil
- AndroidX Biometric

## 项目结构

```text
app/src/main/java/com/example/diary
├─ data
│  ├─ dao
│  ├─ database
│  ├─ entity
│  └─ repository
├─ ui
│  ├─ components
│  ├─ navigation
│  ├─ screens
│  ├─ theme
│  └─ viewmodel
└─ MainActivity.kt
```

## 快速开始

### 环境要求

- Android Studio
- JDK 17
- Android SDK，`compileSdk 34`

### 本地运行

```bash
gradlew.bat assembleDebug
```

也可以直接用 Android Studio 打开项目并运行 `app` 模块。

## 数据设计

项目核心实体包括：

- `DiaryEntry`
- `DiaryEntryFts`
- `DiaryImage`
- `Tag`
- `DiaryTagCrossRef`

其中图片文件保存在应用私有目录，数据库仅保存路径与元信息。

## 当前状态说明

当前仓库已经可以跑通主要记录流程，但仍有一些能力属于“部分完成”状态：

- 应用锁与生物识别设置已经有界面和本地存储，但尚未完成应用启动鉴权闭环
- 搜索已支持全文检索和心情筛选，标签/日期筛选仍未完全打通
- 自动保存设置已存在，但尚未完成真实草稿自动保存逻辑
- 图片排序逻辑已在 `ViewModel` 中预留，界面拖拽排序未完成
- 导出、云同步、备份管理、回收站尚未实现
- 测试仍是默认示例用例

## 开源发布前建议

如果要把这个项目作为 GitHub 开源仓库公开，建议先补齐这些内容：

- 清理构建产物与本地环境文件
- 不提交 `local.properties`
- 不发布 `app-debug.apk`
- 将包名、应用名、图标替换为正式品牌信息
- 补充 `LICENSE`
- 增加应用截图、路线图、贡献说明
- 视情况补充 `CONTRIBUTING.md`、`ISSUE_TEMPLATE`

## Roadmap

- 完成真正的应用锁鉴权流程
- 补全标签/日期筛选
- 增加草稿自动保存
- 增加图片拖拽排序
- 增加导出与备份能力
- 补充测试覆盖率

## 隐私说明

该项目当前以本地优先为主，日记内容与图片默认存储在设备本地。

## 贡献

建议在补齐 License 和基础仓库规范后，再正式对外开放 Issue 和 Pull Request。
