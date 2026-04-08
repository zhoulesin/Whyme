# 英文学习 App 产品需求文档 (PRD)

## 1. 产品概述

### 产品名称
**WhyMe English** - 一款帮助用户系统学习英语的移动应用

### 产品愿景
让英语学习变得有趣、高效、可持续。通过科学的记忆曲线和多样化的学习模式，帮助用户在碎片化时间内实现英语能力的提升。

### 核心价值主张
- **趣味化学习**：游戏化机制让学习不再枯燥
- **个性化路径**：根据用户水平定制学习计划
- **随时随地**：支持离线学习，碎片时间高效利用

---

## 2. 目标用户分析

| 用户画像 | 特征描述 |
|---------|---------|
| 学生党 | 准备考试（ CET-4/6、雅思、托福），需要系统性学习 |
| 职场人士 | 工作中需要英语能力，碎片化时间学习 |
| 英语爱好者 | 出于兴趣学习，追求持续进步 |
| 出国/旅游人群 | 需要实用口语和场景对话能力 |

### 用户痛点
1. 背单词容易遗忘，缺乏科学记忆方法
2. 学习资源分散，难以坚持
3. 缺乏口语练习场景
4. 学习进度难以量化

---

## 3. 核心功能设计

### 3.1 单词学习模块

#### 功能列表
- **单词卡片**：展示单词、音标、例句、配图
- **记忆曲线复习**：基于艾宾浩斯遗忘曲线自动安排复习
- **生词本**：收藏不熟悉的单词进行重点复习
- **单词测试**：中英互译、选择、拼写等多种题型
- **词库级别切换**：支持多级别词库，用户可主动切换学习范围

#### 词库级别系统

**设计背景**：用户群体跨度大（从零基础到高级），不同用户需要不同难度的单词。使用固定词库无法满足个性化需求。

**词库级别定义**：

| 级别 | 名称 | 词汇量 | 适用场景 | 示例词库 |
|------|------|--------|----------|----------|
| L1 | 小学水平 | ~1000词 | 零基础入门 | 人教版小学词汇 |
| L2 | 初中水平 | ~2000词 | 基础巩固 | 人教版初中词汇 |
| L3 | 高中水平 | ~3500词 | 高考备考 | 人教版高中词汇 |
| L4 | 大学四级 | ~4500词 | CET-4 备考 | CET-4 核心词汇 |
| L5 | 大学六级 | ~5500词 | CET-6 备考 | CET-6 核心词汇 |
| L6 | 雅思/托福 | ~8000词 | 留学/移民 | 雅思/托福高频词 |
| L7 | 考研必备 | ~5500词 | 考研备考 | 考研大纲词汇 |
| L8 | 专业英语 | 10000+词 | 商务/学术 | GRE/学术词汇 |

**用户级别选择机制**：
1. **首次使用**：引导用户选择初始级别（通过水平测试或自评）
2. **级别切换**：用户可在设置中随时切换当前学习的词库级别
3. **跨级学习**：用户可同时开启多个级别的词库进行学习
4. **推荐升级**：当用户连续3天完成目标且正确率>85%时，推荐进入下一级别

**学习流程**
```
学习新词 → 理解释义 → 例句应用 → 记忆强化 → 阶段测试
```

**词库切换交互**：
- 入口：首页顶部 / 学习中心顶部
- 切换方式：下拉选择器选择当前学习级别
- 视觉反馈：切换后立即更新学习内容范围
- 进度独立：每个级别的学习进度独立记录

### 3.2 口语练习模块

#### 功能列表
- **跟读练习**：AI评分反馈
- **场景对话**：模拟购物、旅行、面试等真实场景
- **语音评测**：发音准确度分析
- **录音回放**：对比原声纠正发音

### 3.3 阅读理解模块

#### 功能列表
- **分级阅读**：根据难度推荐文章
- **文章精读**：长按查词、划线笔记
- **阅读打卡**：培养每日阅读习惯

### 3.4 学习统计与成就系统

#### 功能列表
- **学习数据看板**：每日/每周/每月学习时长、单词量
- **连续打卡**：激励坚持学习
- **成就徽章**：解锁学习里程碑
- **学习提醒**：自定义提醒时间

---

## 4. 用户界面设计

### 4.1 整体视觉风格
- **设计语言**：Material Design 3
- **主题风格**：简洁、现代、友好
- **配色方案**：
  - 主色：天空蓝 #2196F3
  - 强调色：活力橙 #FF9800
  - 背景色：浅灰白 #FAFAFA
  - 支持深色模式

### 4.2 底部导航结构
```
┌─────────────────────────────────┐
│  首页  │  学习  │  口语  │  我的 │
└─────────────────────────────────┘
```

| Tab | 功能 |
|-----|------|
| 首页 | 今日任务、学习提醒、推荐内容 |
| 学习 | 单词学习、阅读、听力练习 |
| 口语 | 跟读练习、场景对话 |
| 我的 | 个人信息、学习统计、设置 |

### 4.3 关键页面原型

#### 首页
- 今日学习目标进度环
- 待复习单词数量提醒
- 连续打卡天数展示
- 每日一句/英语小知识卡片

#### 单词学习页
- 单词卡片（可翻转）
- 发音按钮
- 例句展示区
- 认识/模糊/不认识 按钮

---

## 5. 技术架构设计

### 5.1 技术栈
| 层级 | 技术选型 |
|------|---------|
| 框架 | Jetpack Compose |
| 架构 | MVVM + Clean Architecture |
| 状态管理 | ViewModel + StateFlow |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 网络 | Retrofit + OkHttp |
| 本地存储 | DataStore Preferences |
| 异步 | Kotlin Coroutines + Flow |

### 5.2 项目结构
```
com.zhoulesin.whyme/
├── data/
│   ├── local/        # 本地数据源
│   ├── remote/       # 远程API
│   └── repository/   # 数据仓库实现
├── domain/
│   ├── model/        # 领域模型
│   ├── repository/   # 仓库接口
│   └── usecase/      # 用例
├── ui/
│   ├── theme/        # 主题样式
│   ├── components/   # 通用组件
│   ├── home/         # 首页
│   ├── learning/     # 学习模块
│   ├── speaking/     # 口语模块
│   └── profile/      # 个人中心
└── di/               # 依赖注入模块
```

### 5.3 数据模型

```kotlin
// 单词实体
data class Word(
    val id: Long,
    val word: String,
    val phonetic: String,
    val definition: String,
    val example: String,
    val translation: String,
    val masteryLevel: Int,  // 0-5 掌握程度
    val wordBank: String,   // 来源词库名称
    val level: WordLevel    // 词库级别枚举
)

// 词库级别枚举
enum class WordLevel(val displayName: String, val description: String) {
    L1_PRIMARY("小学", "小学阶段基础词汇"),
    L2_JUNIOR("初中", "初中阶段核心词汇"),
    L3_SENIOR("高中", "高中阶段必考词汇"),
    L4_CET4("四级", "大学英语四级词汇"),
    L5_CET6("六级", "大学英语六级词汇"),
    L6_IELTS_TOEFL("雅思/托福", "留学考试高频词汇"),
    L7_KAOYAN("考研", "研究生入学考试词汇"),
    L8_GRE("GRE", "美国研究生入学考试词汇")
}

// 用户词库选择
data class UserWordBankSettings(
    val userId: Long,
    val currentLevel: WordLevel,      // 当前学习级别
    val enabledLevels: Set<WordLevel>, // 已开启的级别
    val levelProgress: Map<WordLevel, LevelProgress> // 各级别进度
)

// 级别进度
data class LevelProgress(
    val level: WordLevel,
    val totalWords: Int,              // 词库总词数
    val learnedWords: Int,            // 已学习词数
    val masteredWords: Int,           // 已掌握词数
    val lastStudyDate: LocalDate?      // 最近学习日期
)

// 学习记录
data class LearningRecord(
    val date: LocalDate,
    val wordsLearned: Int,
    val wordsReviewed: Int,
    val duration: Long,  // 秒
    val level: WordLevel // 学习所属级别
)
```

### 5.4 数据库表设计

```sql
-- 单词表
CREATE TABLE words (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word TEXT NOT NULL,
    phonetic TEXT,
    definition TEXT,
    example TEXT,
    translation TEXT NOT NULL,
    word_bank TEXT NOT NULL,
    level TEXT NOT NULL
);

-- 用户词库设置表
CREATE TABLE user_word_bank_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    current_level TEXT NOT NULL DEFAULT 'L3_SENIOR',
    enabled_levels TEXT NOT NULL DEFAULT '["L3_SENIOR"]'
);

-- 用户级别进度表
CREATE TABLE level_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level TEXT NOT NULL UNIQUE,
    total_words INTEGER DEFAULT 0,
    learned_words INTEGER DEFAULT 0,
    mastered_words INTEGER DEFAULT 0,
    last_study_date TEXT
);

-- 用户单词学习进度表（按级别索引）
CREATE TABLE word_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    level TEXT NOT NULL,
    mastery_level INTEGER DEFAULT 0,
    is_learned INTEGER DEFAULT 0,
    is_favorite INTEGER DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    next_review_date TEXT,
    last_review_date TEXT,
    FOREIGN KEY (word_id) REFERENCES words(id),
    UNIQUE(word_id, level)
);
```

---

## 6. MVP 版本规划

### 第一阶段：MVP (4-6周)
**目标**：验证核心学习流程

| 模块 | 功能点 | 优先级 |
|------|--------|--------|
| 单词卡片 | 展示单词、音标、例句 | P0 |
| 单词测试 | 简单的中英互译测试 | P0 |
| 生词本 | 收藏和管理生词 | P1 |
| 学习统计 | 今日/本周学习数据 | P1 |
| **词库级别切换** | **支持多级别词库切换** | **P0** |
| **级别进度管理** | **独立记录每个级别的学习进度** | **P1** |

### 第二阶段：增强 (2-3周)
**目标**：提升学习效果

| 模块 | 功能点 | 优先级 |
|------|--------|--------|
| 记忆曲线 | 基于遗忘曲线的复习提醒 | P0 |
| 成就系统 | 初级徽章和连续打卡 | P1 |
| 每日任务 | 定制化每日学习目标 | P1 |

### 第三阶段：完善 (3-4周)
**目标**：扩展学习场景

| 模块 | 功能点 | 优先级 |
|------|--------|--------|
| 口语跟读 | 基础跟读功能 | P1 |
| 场景对话 | 预设场景练习 | P2 |
| 阅读模块 | 分级文章阅读 | P2 |

---

## 7. 成功指标 (KPIs)

| 指标 | 目标值 | 测量方式 |
|------|--------|----------|
| 日活用户 (DAU) | 1000+ | 启动次数 |
| 次日留存率 | >40% | 第二天回访 |
| 7日留存率 | >20% | 第七天回访 |
| 平均学习时长 | >10分钟/次 | 会话时长 |
| 单词掌握率 | >70% | 测试正确率 |

---

## 8. 后续迭代方向

1. **AI口语助手**：实时对话练习
2. **社交学习**：学习小组、单词对战
3. **会员订阅**：解锁高级课程和专属功能
4. **多语言支持**：拓展至日语、法语等

---

## 附录

### 竞品分析
| 竞品 | 优势 | 劣势 |
|------|------|------|
| 不背单词 | 词根词缀记忆 | 缺乏系统学习路径 |
| 多邻国 | 游戏化程度高 | 深度学习不足 |
| 扇贝单词 | 社区氛围好 | 界面较传统 |

### 参考资源
- 艾宾浩斯遗忘曲线理论
- CEFR 语言能力等级标准
- Material Design 3 设计规范

---

**文档版本**：v2.0  
**创建日期**：2026-04-08  
**更新日期**：2026-04-08  
**负责人**：产品团队  
**状态**：待评审

### 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2026-04-08 | 初始版本，定义核心功能 |
| v2.0 | 2026-04-08 | 新增词库级别系统，支持多级别词库切换 |
