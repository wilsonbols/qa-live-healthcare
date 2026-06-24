# Develop 开发实施规范

## 概述

此规范定义基于 Plan 文档驱动的开发实施流程，供 `qahc-harness-develop` action 参照执行。核心原则：**基于计划、按序推进、闭环验证、人工确认**。

`develop-log.json` 由 `/qahc-harness-plan` 在生成 Plan 文档时一并初始化，确保开发启动时进度文件已就绪。

## 工作区结构

开发过程的状态数据存放于：

```text
.asdm/workspace/qahc-harness/feat/FT-{id}-{name}/
├── develop-log.json                        # 开发进度状态文件
└── ...
```

## 开发状态文件

`develop-log.json` 的完整结构定义参见规范文件 **[develop-log.json](./develop-log.json)**，其中包含每个字段的类型、必填性、约束和示例。

### 核心结构速览

```json
{
  "feature_id": "FT-001",
  "feature_name": "示例特性",
  "plan_path": ".asdm/workspace/qahc-harness/feat/FT-001-name/FT-001-name-Plan.md",
  "current_phase": 1,
  "current_task": "1.3",
  "tasks": {
    "1.1": {
      "name": "任务名",
      "phase": 1,
      "phase_name": "Phase名称",
      "depends_on": [],
      "status": "pending",
      "history": []
    }
  },
  "last_updated": "2026-05-17T04:00:00+08:00"
}
```

### ⚠️ History 条目格式（与旧版不兼容）

history 数组中的每条记录**必须**使用以下四字段结构：

```json
{"timestamp": "2026-05-17T04:17:00+08:00", "from": "pending", "to": "in_progress", "reason": "用户确认开始"}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `timestamp` | string | ISO 8601 带时区偏移 |
| `from` | string | 变更前的状态值 |
| `to` | string | 变更后的状态值 |
| `reason` | string | 变更原因描述 |

> ❌ **禁止使用旧格式** `{"step": "...", "result": "...", "detail": "...", "timestamp": "..."}` — 此格式已废弃。

### 任务状态流转

```text
pending ──→ in_progress ──→ implemented ──→ verifying
                                                  │
                                           ┌──────┴──────┐
                                           ▼              ▼
                                       verified       fixing ──→ verifying
                                           │              ▲        │
                                           ▼              └────────┘
                                    human_confirming
                                           │
                                    ┌──────┴──────┐
                                    ▼              ▼
                                completed       rejected → fixing
```

状态词含义：

| 状态 | 含义 | 触发条件 |
|------|------|----------|
| `pending` | 等待执行 | 初始状态 |
| `in_progress` | 正在实现 | 用户确认进入后 |
| `implemented` | 编码完成 | AI 完成代码编写 |
| `verifying` | 正在验证 | 进入验证步骤 |
| `verified` | 验证通过 | 所有验证步骤通过 |
| `fixing` | 修复中 | 验证失败或人工否决 |
| `human_confirming` | 等待人工确认 | 验证通过后 |
| `completed` | 已完成 | 人工确认通过 |
| `rejected` | 人工拒绝 | 人工确认不通过 |

## 四阶段闭环流程

每个任务必须严格遵循以下流程，缺一不可：

```text
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐
│ 实现     │───→│ 验证     │───→│ 修复     │───→│ 人工确认      │
│ Implement│    │ Verify   │    │ Fix      │    │ Human Confirm │
└──────────┘    └──────────┘    └──────────┘    └──────────────┘
                     │                                 │
                     │ 失败                             │ 否决
                     └─────→ Fixing ◄──────────────────┘
```

### 阶段 1: 实现 (Implement)

- Agent 基于 Plan 文档中该任务的描述进行编码
- 必须读取 PRD 文档和相关 CodeResearch 文件获取完整上下文
- 完成后写入代码，更新状态为 `implemented`

### 阶段 2: 验证 (Verify)

- Agent 严格按照 Plan 文档中该任务的**验证步骤**逐条执行
- 每条验证步骤须有明确的通过/失败结果
- 全部通过 → 状态 `verified`，进入人工确认
- 任何一条失败 → 状态 `fixing`，进入修复阶段

### 阶段 3: 修复 (Fix)

- Agent 分析失败原因并修复代码
- 修复后重新进入验证阶段（见流转图循环）
- **严禁在修复未完成时标注任务为 `completed`**

### 阶段 4: 人工确认 (Human Confirm)

- Agent 展示验证结果摘要
- **必须等待用户明确确认**（"通过"/"确认"/"OK" 等）
- 用户确认 `approved` → 状态 `completed`，更新 Plan 文档
- 用户否决 `rejected` → 状态 `fixing`，返回修复阶段

## Plan 文档状态同步

每个任务 `completed` 后，同步更新 Plan 文档：

1. 更新「进度概要」表中对应任务状态：`⏳` → `✅`
2. 更新当前 Phase 的「目标」描述（如有完成百分比等）
3. 在完成提示中显示剩余任务数

## 会话管理

每个任务完成后，Agent **必须**提示用户：

```text
⚠️ 当前任务已完成，请创建新的 Agent 会话（清空上下文），
在新会话中执行 /qahc-harness-develop FT-{id} 继续下一个任务。

系统将自动定位到下一个待执行任务。
```

**重新进入时的行为**：

1. 读取 `develop-log.json`，定位 `current_task`
2. 若 `current_task` 状态为 `completed`，自动切换到下一个 `pending` 任务
3. 若无 `pending` 任务 → 报告中状态并查找是否有 `rejected` 任务
4. 所有任务 `completed` → 报告特性开发完成

## 错误处理

| 场景 | 处理方式 |
|------|----------|
| Plan 文档不存在 | 终止，提示先执行 `/qahc-harness-plan` |
| `develop-log.json` 不存在 | 从 Plan 文档初始化，所有任务设为 `pending` |
| 任务在 Plan 中被删除 | 标记为已废弃，跳过 |
| 连续修复 3 次仍失败 | 终止当前任务，标记为 `blocked`，向用户报告阻塞原因 |
