# WhyMe English - 英语学习应用

一款基于 Jetpack Compose 构建的 Android 英语学习应用，采用 MVVM + Clean Architecture 架构。

## 功能介绍

### 📖 单词学习
- **单词卡片**：展示单词、音标、例句和中文释义，支持点击翻转
- **记忆曲线复习**：基于艾宾浩斯遗忘曲线自动安排复习时间
- **收藏功能**：收藏不熟悉的单词，方便重点复习
- **学习进度**：实时显示今日学习进度

### 🔄 学习模式
- **新词学习**：每日定量学习新单词
- **复习模式**：根据记忆曲线自动推送需要复习的单词
- **测试模式**：选择题形式检验学习成果

### 📊 数据统计
- **学习数据看板**：累计单词数、学习时长、正确率
- **连续打卡**：记录每日学习情况，激励坚持
- **成就系统**：解锁学习里程碑徽章

### 🎨 界面特色
- **Material Design 3**：遵循最新设计规范
- **深色模式**：支持跟随系统或手动切换
- **底部导航**：首页、学习、我的 三Tab简洁导航

## 技术栈

| 技术 | 说明 |
|------|------|
| Jetpack Compose | 现代声明式 UI 框架 |
| Hilt | 依赖注入 |
| Room | 本地数据库 |
| DataStore | 轻量级键值存储 |
| Navigation Compose | 页面导航 |
| Kotlin Coroutines + Flow | 异步编程 |

## 项目结构

```
com.zhoulesin.whyme/
├── data/                    # 数据层
│   ├── local/              # 本地数据源 (Room, DataStore)
│   └── repository/         # Repository 实现
├── domain/                  # 领域层
│   ├── model/              # 数据模型
│   ├── repository/         # 仓库接口
│   └── usecase/            # 业务用例
├── di/                      # 依赖注入模块
└── ui/                      # 表现层
    ├── components/         # 通用组件
    ├── home/               # 首页
    ├── learning/           # 学习模块
    ├── navigation/         # 导航配置
    ├── profile/            # 个人中心
    └── theme/              # 主题样式
```

## 快速开始

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 35

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

## 后续规划

- [ ] TTS 语音发音功能
- [ ] 艾宾浩斯记忆曲线完整实现
- [ ] 成就系统完整解锁逻辑
- [ ] 口语跟读练习
- [ ] 场景对话模拟
- [ ] 分级阅读模块

## 截图预览

> 截图待添加

## License

MIT License
