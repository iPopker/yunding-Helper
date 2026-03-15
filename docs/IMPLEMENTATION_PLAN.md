# jccHelper 实施计划（MVP v1）

## 1. 文档目的

本文件用于指导 `jccHelper` 从 0 到 1 落地。后续开发默认按本计划推进，除非有新决策覆盖。

## 当前进度（2026-03-15）

- M0 已完成：
  - 依赖收敛为 `Spring Boot 3.x + jOOQ + SQLite`
  - 已提供 `GET /api/v1/health`
  - 已提供 `POST /api/v1/analyze`（mock）
  - 已接入请求级 `traceId`（日志 MDC + 响应头）
  - 已补充基础接口测试并通过
- M1 已完成：
  - 已建立 `V1__init_m1_tables.sql`，覆盖 `game_session/turn_state/state_diff/memory_summary/retrieval_trace/advice_trace`
  - `analyze` 链路已实现 `Observation -> GameState -> StateDiff -> GameMemory` 并持久化
  - 同一 `gameId` 连续上传会自动递增 `turnIndex` 并基于上一回合计算 `diff`
  - 已提供 `GET /api/v1/games/{gameId}/diffs` 查询回合差分
  - 已补充连续回合测试并通过
- M2 已完成：
  - 已接入 RAG 链路：`QueryPlanner -> KnowledgeRetriever(Qdrant + fallback) -> RetrievalTrace`
  - 已实现 `retrieval_trace` 持久化与查询接口 `GET /api/v1/games/{gameId}/retrievals`
  - 已增加结构化建议约束：`StructuredAdvice` + `StructuredAdviceValidator`
  - `analyze` 已按结构化对象生成并校验后返回
  - 测试通过，确认连续回合时检索轨迹可落库并可查询
- M3 已完成：
  - 已提供回放接口 `GET /api/v1/games/{gameId}/replay`，可按回合查看 state/diff/memory/retrieval/advice
  - 已提供质量指标接口 `GET /api/v1/metrics/quality`（识别率、低置信度比例、建议稳定性、P50/P95 延迟）
  - `analyze` 已接入低置信度兜底策略（阈值 0.68），低置信度时返回保守建议并显式不确定性
  - 已补充 M3 测试并通过
- 下一步：M4 体验收敛与真实视觉/知识数据接入

---

## 2. 项目目标与边界

### 2.1 MVP 目标

- 输入：单张 TFT 截图 + `gameId`
- 输出：
  - 当前局势摘要
  - 推荐阵容方向
  - 本回合建议动作
  - 主要原因
  - 风险与不确定性
- 能力：同一局内维护短期记忆，保证建议连续性

### 2.2 第一阶段不做（Non-goals）

- 自动点击操作游戏
- 实时视频流分析
- 自动侦察对手
- 全自动复盘
- 多局长期画像
- 高精度全棋盘视觉训练

---

## 3. 架构与模块

### 3.1 架构原则

- 代码主导工作流，LLM 仅辅助关键节点
- Java 服务显式编排，不做全自主 Agent
- 所有关键中间结果可落库、可回放、可审计

### 3.2 模块划分

1. `vision-adapter`
- 截图裁切、OCR、模板识别
- 输出统一 `Observation`

2. `state-engine`
- `Observation -> GameState`
- `GameState(previous, current) -> StateDiff`
- 维护 `GameMemory`

3. `rag-engine`
- 知识入库（chunk + embedding）
- Qdrant 检索与 metadata filter
- 为建议提供证据上下文

4. `planner`
- 生成候选方案（阵容方向、行动建议）
- 评分与排序（经济、血量、成型速度、风险）

5. `advice-service`
- LangChain4j structured output
- 汇总最终建议并落库

6. `trace-replay`
- 按 `gameId` 回放每回合状态与建议

---

## 4. 里程碑与交付件

## M0：工程基建（第 1 周）

### 目标

建立可运行后端骨架与基础环境。

### 任务

1. 固定依赖策略
- Spring Boot 版本统一（建议与设计文档对齐到 3.x，或正式确认升级到 4.x）
- ORM 二选一：`jOOQ` 或 `MyBatis-Plus`（默认建议 `jOOQ`）

2. 基础设施接入
- SQLite 连接与本地文件路径配置
- Qdrant 本地开发环境（Docker）
- 日志 traceId 贯通

3. API 骨架
- `GET /api/v1/health`
- `POST /api/v1/analyze`（先返回 mock）

### 验收标准

- 项目可启动
- health 可调用
- analyze 接口可收图并返回固定结构

---

## M1：状态链路 MVP（第 2 周）

### 目标

打通截图到状态与记忆更新的主流程。

### 任务

1. 定义核心模型（Java + DB）
- `GameState`
- `StateDiff`
- `GameMemory`
- `AdviceResult`

2. 建表与迁移
- `game_session`
- `turn_state`
- `state_diff`
- `memory_summary`
- `advice_trace`
- `retrieval_trace`

3. 编排主链路
- `VisionAdapter.analyze(image) -> Observation`
- `StateEngine.build(observation) -> GameState`
- `StateEngine.diff(prev, curr) -> StateDiff`
- `MemoryEngine.update(gameId, state, diff)`

### 验收标准

- 同一 `gameId` 连续上传 2~3 张截图可产生连续状态
- 数据可完整落库
- 可查询到每回合 diff

---

## M2：RAG + 建议生成 MVP（第 3 周）

### 目标

接入知识检索与结构化建议输出。

### 任务

1. 知识库流程
- 文档切分与 metadata 设计（赛季版本、阵容标签、装备标签）
- embedding 入 Qdrant
- 检索接口（TopK + filter）

2. LLM 结构化输出
- 定义输出 schema（JSON）
- 通过 LangChain4j AI Service 返回强约束结构

3. 决策组装
- 输入：`GameState + StateDiff + GameMemory + RetrievalContext`
- 输出：`AdviceResult`

### 验收标准

- 每次建议都带“理由 + 风险 + 不确定性”
- 结构化 JSON 可稳定反序列化
- 检索命中可追踪（保存 query 与命中文档）

---

## M3：可观测与回放（第 4 周）

### 目标

具备诊断、复现和迭代优化能力。

### 任务

1. 回放接口
- `GET /api/v1/games/{gameId}/replay`

2. 指标
- 字段识别成功率
- 建议生成耗时（P50/P95）
- 建议稳定性（相邻回合波动）
- 低置信度比例

3. 兜底策略
- 识别不全时降级输出“保守建议”
- 高不确定性时显式提示“需人工确认”

### 验收标准

- 任一局可按时间线重放
- 可定位某回合建议依据
- 失败案例可复现并迭代

---

## 5. 数据模型草案（MVP）

### 5.1 GameState（建议字段）

- `gameId`
- `turnIndex`
- `stage`（如 `3-2`）
- `gold`
- `level`
- `hp`
- `shopUnits`（列表）
- `items`（列表）
- `boardUnits`（弱识别可空）
- `benchUnits`（弱识别可空）
- `augments`（可空）
- `confidence`（整体置信度）

### 5.2 StateDiff（建议字段）

- `gameId`
- `fromTurn`
- `toTurn`
- `goldDelta`
- `hpDelta`
- `levelDelta`
- `shopChanged`
- `boardChanged`
- `benchChanged`
- `itemChanged`
- `summary`

### 5.3 AdviceResult（建议字段）

- `summary`
- `compDirection`
- `actions`（按优先级排序）
- `reasons`
- `risks`
- `uncertainties`
- `confidence`

---

## 6. API 契约（MVP 草案）

## POST `/api/v1/analyze`

### Request

- `gameId`（string）
- `image`（multipart file）

### Response（示例结构）

```json
{
  "gameId": "g_001",
  "turnIndex": 3,
  "summary": "当前血量健康，经济一般，建议优先稳血。",
  "compDirection": "前排斗士 + 后排法系过渡",
  "actions": [
    "优先提升前排质量，保留关键对子",
    "本回合不强行D牌，保留经济到下一关键节点"
  ],
  "reasons": [
    "当前战力不足但血量尚可",
    "商店命中率不足以支撑强行转阵"
  ],
  "risks": [
    "若下回合仍弱势，可能出现连续掉血"
  ],
  "uncertainties": [
    "棋盘识别置信度较低，部分站位可能有误"
  ],
  "confidence": 0.72
}
```

---

## 7. 开发规范

1. 所有链路必须可追踪
- 每次 analyze 请求生成统一 `traceId`
- 关键步骤都记录输入摘要、输出摘要、耗时

2. 严格结构化输出
- LLM 输出必须经过 schema 校验
- 校验失败走重试与兜底模板

3. Prompt 与策略版本化
- 记录 `promptVersion`、`knowledgeVersion`
- 方便回归对比

4. 先通路，再精度
- 第一优先级是链路闭环，不追求视觉完美识别

---

## 8. 风险清单与应对

1. 风险：视觉识别不稳定
- 应对：字段级置信度 + 缺失容错 + 保守建议模板

2. 风险：建议不连贯
- 应对：强制读取最近 N 回合 memory summary + diff

3. 风险：RAG 命中偏差
- 应对：metadata 过滤 + 检索轨迹落库 + 人工抽样评估

4. 风险：技术栈漂移
- 应对：尽快锁定 Spring Boot 与 ORM 方案并固化

---

## 9. 执行顺序（默认）

1. 锁定技术栈（Boot 版本 + ORM）
2. 建立 DB schema 与 migration
3. 搭建 `analyze` 主链路（先 mock Vision）
4. 接入 LangChain4j structured output
5. 接入 Qdrant RAG
6. 完成可观测与回放
7. 用真实截图集迭代精度

---

## 10. 当前待确认决策

1. Spring Boot 最终版本（3.x or 4.x）
2. ORM 选择（jOOQ or MyBatis-Plus）
3. Vision 服务形态（同仓模块 or 独立服务）
4. 知识源首批范围（仅基础运营策略，还是包含主流阵容库）

> 未确认前，默认：`Spring Boot 3.x + jOOQ + Vision 同仓适配层 + 基础策略知识库`。
