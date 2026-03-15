# TFT Screenshot Coach Agent Design

## 1. 项目概述

### 1.1 项目目标

构建一个面向《云顶之弈》的**截图决策辅助 Agent**。系统输入一张游戏截图，输出当前局势理解、阵容方向建议、本回合动作建议、风险提示，并在同一局内维护短期记忆，使后续建议具备连续性。

### 1.2 核心目标

本项目主要用于锻炼以下 Agent 开发能力：

- 状态建模（State Modeling）
- 状态差分（State Diff）
- 局内记忆（Working Memory / Episodic Memory）
- 检索增强生成（RAG）
- 候选方案规划（Planning / Candidate Generation）
- 结构化输出（Structured Output）
- 多模块编排（Orchestration）
- 可观测性与可回放（Traceability / Replayability）

### 1.3 非目标

第一阶段不做以下内容：

- 自动点击操作游戏
- 实时视频流分析
- 自动侦察对手
- 全自动复盘
- 多局长期用户画像
- 高精度全棋盘视觉识别训练

---

## 2. MVP 边界

### 2.1 输入

- 单张游戏截图
- `gameId`：同一局内多次上传时用于关联历史状态

### 2.2 输出

- 当前局面摘要
- 推荐阵容方向
- 本回合建议动作
- 主要原因
- 风险和不确定性

### 2.3 第一阶段识别字段

系统优先识别以下字段：

- `stage`：当前阶段，如 `3-2`、`4-1`
- `gold`
- `level`
- `hp`
- `shopUnits`
- `items`
- `boardUnits`（可先弱识别）
- `benchUnits`（可先弱识别）
- `augments`（可选）

---

## 3. 总体架构

### 3.1 架构原则

本系统采用“**代码主导工作流 + LLM 辅助关键节点**”的设计：

- 主流程由 Java 服务显式编排
- LLM 不负责全权自治
- LangChain4j 主要用于：
    - AI Services
    - Structured Output
    - Embedding / RAG 集成
    - Query Planning
    - Memory Summary
    - Final Advice Generation

### 3.2 架构分层

#### A. Vision Tool Service

职责：

- 截图裁切
- OCR
- 模板匹配 / 图标识别
- 输出 Observation

说明：
- 不纳入 LangChain4j 主体逻辑

#### B. Java Orchestrator

职责：

- 调用视觉服务
- 构建 `GameState`
- 计算 `StateDiff`
- 更新 `GameMemory`
- 调用 RAG
- 构建候选方案
- 生成最终建议
- 落库

#### C. SQLite

职责：

- 存储运行状态
- 存储回合状态
- 存储局内记忆
- 存储检索轨迹
- 存储决策轨迹
- 存储最终建议

#### D. Qdrant

职责：

- 存储知识 chunk embedding
- 提供语义检索
- 支持 metadata filter

#### E. LangChain4j Layer

职责：

- 接入聊天模型
- 接入 embedding 模型
- 定义 AI Services
- 管理 structured output
- 参与 RAG query planning
- 生成 memory summary 和 final advice

---

## 4. 技术选型

### 4.1 主系统

- Java 21
- Spring Boot 3.x

### 4.2 ORM / 数据访问

推荐二选一：

- MyBatis-Plus
- jOOQ

优先建议：

- 如果偏业务开发习惯：MyBatis-Plus
- 如果偏 SQL 可控性：jOOQ

### 4.3 数据库

- SQLite
- JDBC Driver：`sqlite-jdbc`

### 4.4 LLM / Agent 框架

- LangChain4j

建议使用能力：

- AI Services
- Structured Output
- EmbeddingModel
- EmbeddingStore
- 局部 Tool Calling

### 4.5 向量库

- Qdrant

### 4.6 视觉服务
- OpenCV

---

## 5. 核心领域模型

### 5.1 GameState

表示当前回合的结构化游戏状态。
