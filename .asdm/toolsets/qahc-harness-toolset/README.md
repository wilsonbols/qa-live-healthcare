# ASDM Toolset - QA Healthcare 驾驭工程工具集

## 概述

QA Healthcare 驾驭工程工具集（`qahc-harness-toolset`）是一套面向 [QA Healthcare](/) 医疗问答系统的全流程 AI 辅助软件工程工具集，基于 **[ASDM](https://asdm.ai) — AI 优先的系统开发方法论** 构建。

### 关于 ASDM

[ASDM](https://asdm.ai)（AI-First System Development Methodology & Platform）是软通动力（iSoftStone）推出的 AI 原生数字化研发平台。其核心设计理念是软件工程范式的根本性转变——从传统的「为人设计工具和流程」升级为「为 AI 设计工具和流程」，让 AI 如同专业开发者一样深度参与研发全流程。

ASDM 具备 **三大核心能力**：

| 能力 | 核心目标 | 本工具集的落地体现 |
| ------ | ---------- | ------------------- |
| **场景化认知** | 为 AI 搭建精准的上下文环境，让 AI 理解项目需求、业务逻辑与编码规范 | 项目上下文注入、医疗领域知识封装、编码规范自动校验 |
| **自闭环执行** | 让 AI 拥有可靠的校验与修正能力，最小化人工介入 | AI 生成 → 自动测试 → 反馈修正 → 重新生成的完整闭环 |
| **自适应迭代演进** | 保障 AI 研发体系随项目发展持续适配 | 上下文动态更新、工具链版本同步、规范迭代升级 |

### 本工具集的定位

作为 QA Healthcare 项目的驾驭工程（Harness Engineering）核心工具集，它围绕 `模型 + 驾驭层 → 智能体` 的理念，构建 AI Agent 运行所需的完整执行环境——包括上下文注入、工具调用、约束规则、反馈回路和编排系统。目标是在医疗问答系统这一特定垂直领域，建立稳定、可靠、可控的 AI 优先研发流水线。

用户可将此工具集安装到工作区，通过 `INSTALL.md` 中的「AI 引导安装」流程初始化，随后通过 ASDM 命令系统驱动 AI Agent 完成各项研发任务。

## 功能特性

### 通用能力

- **ASDM 方法论驱动**：所有功能严格遵循 [ASDM 方法论](https://asdm.ai/docs) 的核心原则——场景化认知、自闭环执行、自适应迭代
- **全流程覆盖**：从需求分析到底层编码、从测试验证到部署运维，贯穿软件研发生命周期的每一个阶段
- **医疗领域适配**：内置医疗问答领域上下文与知识模板，使 AI Agent 准确理解业务语义
- **驾驭工程基座**：提供上下文管理、工具编排、反馈控制等 Harness 层基础能力，使 AI Agent 在 QA Healthcare 项目中稳定、可控地工作
- **平台兼容**：工具集产出天然兼容 [ASDM Platform](https://platform.asdm.ai)，支持以 HSCP（Harness System Configuration Package）形式部署

### 功能一：需求分析与任务规划（Ask → Design → Plan）

提供从需求澄清到可执行开发计划的四阶段渐进式需求工程流水线：

- **澄清阶段（`/qahc-harness-askme`）**：通过结构化追问，消除需求描述中的模糊点、歧义和缺失信息
- **总体设计阶段（`/qahc-harness-design-overall`）**：基于澄清结果，生成 PRD 核心章节（总体概述 + 使用场景）
- **详细设计阶段（`/qahc-harness-design-details`）**：基于总体设计进行 Code Research，补充技术方案、数据模型、接口设计、依赖关系、风险与缓解、验收条件 (DoD) 等详细章节
- **规划阶段（`/qahc-harness-plan`）**：基于完整的 PRD 文档，拆解为有序、可估、可追踪的开发任务清单

- **输入**：用户自然语言需求描述
- **输出**：需求澄清记录 → PRD 总体设计 → PRD 详细设计 → 可执行开发计划
- **使用场景**：新功能启动、需求评审、需求变更评估

### 功能二：上下文感知与质量保障

基于 `.asdm/contexts/` 中的项目上下文知识库（由 `qahc-context-builder` 工具集维护），确保 AI Agent 在每个阶段都能准确理解项目现状、技术栈规范与业务约束，并通过自动校验闭环守卫产出质量。

- **输入**：代码变更请求或任务描述
- **输出**：符合项目规范、通过自动质量校验的代码产出及校验报告
- **使用场景**：常规开发任务、代码审查、Bug 修复

### 功能三：驾驭层配置与编排

为 QA Healthcare 项目的多微服务架构配置 AI Agent 运行环境——定义工作区布局、工具调用策略、反馈回路机制以及多 Agent 协作编排规则。

- **输入**：项目架构信息（微服务拓扑、端口配置、依赖关系等）
- **输出**：AI Agent 可识别的驾驭层配置与执行策略
- **使用场景**：新成员接入项目、环境迁移、Agent 行为调优

## 依赖 Skills

本工具集依赖以下 ASDM skills 提供底层能力支撑：

| Skill | 用途 |
|--------|------|
| `context-loader` | 加载项目上下文和知识库，帮助 AI Agent 准确理解项目现状、技术栈规范与业务约束 |
| `path-validator` | 验证文件路径和引用有效性，确保文档中的链接和引用准确无误，支持跨文件、跨模块的路径校验 |
| `markdownlint` | 对 Markdown 文档进行 lint 检查，确保 PRD、Plan、TestPlan 等产出的文档格式规范和质量一致 |

### 安装方式

以上 skills 已纳入 INSTALL.md 的自动安装流程，按照 INSTALL.md 引导安装时会自动执行安装。如需单独安装，也可使用 `asdm` CLI 手动执行：

```bash
asdm skill install context-loader
asdm skill install path-validator
asdm skill install markdownlint
```

## 安装说明

`INSTALL.md` 通过以下步骤配置工具集：

1. 检测当前 AI 助手运行环境（CodeBuddy / Claude Code / GitHub Copilot / Trae / Qoder / OpenCode）
2. 验证前置依赖——确保 `qahc-context-builder` 工具集已安装并生成项目上下文
3. 在命令注册目录创建工具集快捷方式
4. 复制 `actions/` 目录下的动作指令文件到命令目录
5. 初始化工作区目录结构

## 支持的代码助手

本工具集已适配以下 AI 代码助手：

- Tencent CodeBuddy
- Claude Code
- GitHub Copilot
- Trae
- Qoder
- OpenCode

详细安装步骤请参考 INSTALL.md。

## 使用流程

安装完成后，用户可以使用以下命令驱动 AI Agent 完成研发任务：

| 命令 | 用途 | 适用阶段 |
| ------ | ------ | ---------- |
| `/qahc-harness-init` | 初始化驾驭工作区——生成总体计划（overall-plan）与工作区目录骨架 | 项目启动 |
| `/qahc-harness-askme` | 需求澄清——通过结构化追问，消除需求中的模糊点和歧义 | 需求分析 |
| `/qahc-harness-design-overall` | 基于澄清结果，输出 PRD 总体设计（总体概述 + 使用场景） | 总体设计 |
| `/qahc-harness-design-details` | 基于总体设计进行 Code Research，输出 PRD 详细设计（技术方案、数据模型、接口设计、DoD 等） | 详细设计 |
| `/qahc-harness-plan` | 根据完整 PRD 文档，输出可执行的开发计划（Plan），含 develop-log.json | 任务规划 |
| `/qahc-harness-develop` | 启动 AI 优先编码流程，自动编码并校验 | 编码实现 |
| `/qahc-harness-testplan` | 生成功能测试计划——PRD 驱动的测试套件、用例与步骤 | 测试计划 |
| `/qahc-harness-deploy` | （后续实现）AI 辅助的构建、部署与健康检查 | 发布运维 |
| `/qahc-harness-verify` | 功能验收审查——对照 PRD 的 DoD 生成验收报告 | 验收审查 |

**推荐工作流**：

1. 项目启动 → 执行 `/qahc-harness-init` 初始化驾驭工作区与总体计划
2. 需求澄清 → 执行 `/qahc-harness-askme` 消除需求歧义
3. 总体设计 → 执行 `/qahc-harness-design-overall` 生成 PRD 总体设计（总体概述 + 使用场景）
4. 详细设计 → 执行 `/qahc-harness-design-details` 进行 Code Research，生成完整 PRD（技术方案、数据模型、接口设计、DoD）
5. 任务规划 → 执行 `/qahc-harness-plan` 基于 PRD 拆解开发任务，生成 develop-log.json
6. 编码实现 → 执行 `/qahc-harness-develop` 启动自动编码闭环
7. 测试验证 → 执行 `/qahc-harness-testplan` 生成功能测试计划并执行
8. 代码审查 → 执行 `/qahc-harness-verify` 生成功能验收报告
9. 构建发布 → 执行 `/qahc-harness-deploy`（后续实现）

## 工具集结构

QA Healthcare 驾驭工程工具集的目录结构如下：

```text
.asdm/
└── toolsets/
    └── qahc-harness-toolset/
        ├── INSTALL.md                     # 工具集安装说明（AI 引导安装流程）
        ├── README.md                      # 本文件 — 工具集说明文档
        ├── COMPLETION_REPORT.md           # 工具集完成报告
        ├── actions/                       # 动作指令定义
        │   ├── qahc-harness-init.md       # 初始化驾驭工作区——生成总体计划与目录骨架
        │   ├── qahc-harness-askme.md      # 需求澄清——结构化追问消除需求歧义
        │   ├── qahc-harness-design-overall.md   # PRD 总体设计——生成总体概述 + 使用场景
        │   ├── qahc-harness-design-details.md   # PRD 详细设计——Code Research + 技术方案、数据模型、接口、DoD
        │   ├── qahc-harness-plan.md       # 开发任务拆解——基于 PRD 纵向拆分，生成可执行 Plan + develop-log.json
        │   ├── qahc-harness-develop.md    # 开发实施——基于 Plan 按序执行，实现→验证→修复→人工确认闭环
        │   ├── qahc-harness-testplan.md   # 功能测试计划——基于 PRD 输出 TestPlan（套件+用例+步骤）
        │   └── qahc-harness-verify.md     # 功能验收审查——对照 PRD/DoD 生成验收报告
        └── specs/                         # 规范定义
            ├── overall-plan-spec.md       # 总体计划模板 — 定义 overall-plan.md 的结构与填充规则
            ├── prd-spec.md                # PRD 文档规范 — 定义产品需求文档的结构与质量标准
            ├── plan-spec.md               # Plan 文档规范 — 定义开发计划的拆解粒度与内容要求
            ├── develop-spec.md            # 编码规范 — 约束 AI 编码产出质量
            ├── develop-log.json           # 开发日志规范 — 定义开发进度跟踪文件的标准格式
            ├── test-spec.md               # 测试规范 — 定义测试标准与覆盖率要求
            └── review-spec.md             # 审查规范 — 定义代码审查检查项与标准
```

## 工作区结构

QA Healthcare 驾驭工程工具集使用以下工作区结构存储运行时数据：

```text
.asdm/workspace/qahc-harness/
├── overall-plan.md                        # ★ 项目总体计划 — 涵盖模块划分、迭代信息、特性清单
├── feat/                                  # 特性工作目录
│   └── FT-{id}-{name}/                    # 单个特性全流程文档
│       ├── FT-{id}-{name}-AskMe.md        # 需求访谈追问文档（决策点+回答记录）
│       ├── FT-{id}-{name}-PRD.md          # PRD 产品需求文档（总体概述+使用场景+技术方案+DoD）
│       ├── FT-{id}-{name}-CodeResearch-{module}.md  # 代码调研总结（每模块一个）
│       ├── FT-{id}-{name}-Plan.md         # 开发任务计划（可执行任务拆解）
│       └── FT-{id}-{name}-develop-log.json  # 开发进度跟踪（任务状态、完成情况）
```

## 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v0.0.8 | 2026-05-28 | 新增「依赖 Skills」章节及 `skills` manifest 声明；INSTALL.md 增加 asdm CLI 安装 skills 步骤；移除文档内部链接 |
| v0.0.7 | 2026-05-28 | 新增「支持的代码助手」章节；扩展安装适配范围，新增 Trae、Qoder、OpenCode 三种代码助手支持；同步更新 INSTALL.md |
| v0.0.6 | 2026-05-28 | 初始版本，支持 CodeBuddy、Claude Code、GitHub Copilot |

## 版权与许可

Copyright (c) 2026 LeansoftX.com & iSoftStone. All rights reserved.

Licensed under the PROPRIETARY SOFTWARE LICENSE. See [LICENSE](LICENSE) in the project root for license information.
