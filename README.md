# WhyMe English - 英语学习应用

一款基于 Jetpack Compose 构建的 Android 英语学习应用，采用 MVVM + Clean Architecture 架构，遵循 Linear Design System 设计规范。

## 功能介绍

### 📖 单词学习
- **单词卡片**：展示单词、音标、例句和中文释义，支持点击翻转
- **记忆曲线复习**：基于艾宾浩斯遗忘曲线自动安排复习时间
- **收藏功能**：收藏不熟悉的单词，方便重点复习
- **单词详情**：从收藏页点击单词可查看详情

### 🔄 学习模式
- **新词学习**：每日定量学习新单词
- **复习模式**：根据记忆曲线自动推送需要复习的单词
- **测试模式**：选择题形式检验学习成果

### 📊 数据统计
- **学习数据看板**：累计单词数、学习时长、正确率
- **连续打卡**：记录每日学习情况，激励坚持
- **本周趋势**：可视化展示本周学习进度

### 🎨 界面特色
- **Linear Design System**：深色主题，高对比度，现代化视觉风格
- **Material Design 3**：遵循最新设计规范
- **底部导航**：首页、学习、我的 三Tab简洁导航

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Jetpack Compose | BOM 2024.02.00 | 现代声明式 UI 框架 |
| Material 3 | - | Material Design 组件 |
| Hilt | 2.51 | 依赖注入 |
| Room | 2.6.1 | 本地数据库 |
| DataStore | 1.0.0 | 轻量级键值存储 |
| Navigation Compose | 2.7.7 | 页面导航 |
| Kotlin Coroutines + Flow | 1.8.0 | 异步编程 |

## 项目结构

```
com.zhoulesin.whyme/
├── data/                    # 数据层
│   ├── local/              # 本地数据源
│   │   ├── database/       # Room 数据库
│   │   ├── datastore/      # DataStore 存储
│   │   └── entity/         # 数据库实体
│   └── repository/         # Repository 实现
├── domain/                  # 领域层
│   ├── model/              # 数据模型
│   ├── repository/         # 仓库接口
│   └── usecase/            # 业务用例
├── di/                      # 依赖注入模块
└── ui/                      # 表现层
    ├── components/         # 通用组件
    │   └── WordCard.kt     # 单词卡片组件
    ├── favorites/          # 收藏模块
    ├── home/               # 首页
    ├── learning/           # 学习模块
    │   ├── LearningScreen.kt
    │   ├── NewWordLearningScreen.kt
    │   ├── ReviewScreen.kt
    │   ├── QuizScreen.kt
    │   ├── StudySessionScreen.kt
    │   ├── ReviewSessionScreen.kt
    │   ├── QuizSessionScreen.kt
    │   ├── WordDetailScreen.kt
    │   └── WordDetailViewModel.kt
    ├── navigation/         # 导航配置
    ├── profile/            # 个人中心
    ├── statistics/         # 统计页面
    └── theme/              # 主题样式
        ├── Color.kt        # Linear Design 颜色系统
        ├── Theme.kt        # 主题配置
        └── Type.kt         # 字体配置
```

## 设计系统

应用采用 **Linear Design System** 设计规范：

### 颜色系统
- **背景色**：MarketingBlack (#08090A)、PanelDark (#0F1011)、Level3Surface (#191A1B)
- **文本色**：PrimaryText (#F7F8F8)、SecondaryText (#D0D6E0)、TertiaryText (#8A8F98)
- **品牌色**：BrandIndigo (#5E6AD2)、AccentViolet (#7170FF)

### 组件规范
- 使用 Surface 替代 Card，配合 BorderStroke 实现边框效果
- 按钮使用 BrandIndigo 作为主色
- 所有页面统一使用 MarketingBlack 作为背景

## 快速开始

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 35
- minSdk 26

### 构建运行

```bash
# 克隆项目
git clone <repository-url>
cd whyme

# 构建 Debug 版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

## 已实现功能

- ✅ 单词卡片翻转效果
- ✅ 新词学习流程
- ✅ 复习模式（基于艾宾浩斯曲线）
- ✅ 测试模式（选择题）
- ✅ 收藏功能
- ✅ 单词详情页
- ✅ 学习数据统计
- ✅ 本周趋势图表
- ✅ Linear Design System 视觉风格
- ✅ 深色主题

## 后续规划

- [ ] TTS 语音发音功能
- [ ] 艾宾浩斯记忆曲线完整实现
- [ ] 成就系统完整解锁逻辑
- [ ] 口语跟读练习
- [ ] 场景对话模拟
- [ ] 分级阅读模块
- [ ] 多词库支持
- [ ] 数据备份与同步

## 版本历史

### v1.0.0
- 初始版本发布
- 实现基础学习功能
- 完成 Linear Design System 视觉升级

## License

MIT License
