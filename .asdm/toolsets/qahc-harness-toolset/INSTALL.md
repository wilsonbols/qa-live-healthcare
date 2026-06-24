# QA Healthcare 驾驭工程工具集 安装说明

**Toolset ID:** `qahc-harness-toolset`

## 概述
本文档提供 QA Healthcare 驾驭工程工具集的安装与配置说明。该工具集为 QA Healthcare 医疗问答系统项目提供完整的 AI 辅助软件工程支持，覆盖从需求分析、设计、编码、测试到验收审查的全流程。

## AI 引导安装
如需使用 AI 引导安装，请将以下提示复制粘贴到 AI 编码工具的聊天窗口：

```shell
Follow instructions in .asdm/toolsets/qahc-harness-toolset/INSTALL.md
```

## 安装步骤

### 1. 创建工作区目录

创建 QA Healthcare 驾驭工作区的目录结构：

```bash
mkdir -p .asdm/workspace/qahc-harness/feat
```

### 2. 检测当前 `Agentic Engine` 提供商

检测当前 AI 编码助手提供商（如 Claude Code、GitHub Copilot、Tencent CodeBuddy、Trae、Qoder、OpenCode），按以下规则判断：

- 如果 `.claude` 目录存在，使用 `Claude Code`
- 如果 `.github` 目录存在，使用 `GitHub Copilot`
- 如果 `.codebuddy` 目录存在，使用 `Tencent CodeBuddy`
- 如果 `.trae` 目录存在，使用 `Trae`
- 如果 `.qoder` 目录存在，使用 `Qoder`
- 如果 `.opencode` 目录存在，使用 `OpenCode`
- 如果以上目录均不存在，提示用户手动选择提供商

### 3. 为 QA Healthcare 驾驭工程工具集（toolset ID: `qahc-harness-toolset`）创建快捷命令

> **设计原则**：命令文件应为**薄引用**（单行 `follow` 指令），指向 `.asdm/toolsets/qahc-harness-toolset/actions/` 下的实际 action 文件。这样无需重新安装即可获得 action 文件更新。

#### 对于 Tencent CodeBuddy（`.codebuddy/commands/`）：

CodeBuddy 命令使用简化的单行格式 —— 仅包含指向 action 文件的 `follow` 指令：

```bash
mkdir -p .codebuddy/commands/

# 工作区初始化
cat > .codebuddy/commands/qahc-harness-init.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md
EOF

# 需求访谈追问
cat > .codebuddy/commands/qahc-harness-askme.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
EOF

# PRD 总体设计
cat > .codebuddy/commands/qahc-harness-design-overall.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md
EOF

# PRD 详细设计
cat > .codebuddy/commands/qahc-harness-design-details.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md
EOF

# 开发任务拆解
cat > .codebuddy/commands/qahc-harness-plan.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md
EOF

# 开发实施
cat > .codebuddy/commands/qahc-harness-develop.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md
EOF

# 测试计划生成
cat > .codebuddy/commands/qahc-harness-testplan.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md
EOF

# 功能验收审查
cat > .codebuddy/commands/qahc-harness-verify.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md
EOF
```

#### 对于 Claude Code（`.claude/commands/`）：

Claude Code 需 Markdown 前置元数据。创建命令时依次写入前置元数据和 `follow` 指令：

```bash
mkdir -p .claude/commands/

# 工作区初始化
cat > .claude/commands/qahc-harness-init.md << 'EOF'
---
description: "初始化驾驭工作区，创建目录骨架并生成项目总体计划"
argument-hint: ""
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md
EOF

# 需求访谈追问
cat > .claude/commands/qahc-harness-askme.md << 'EOF'
---
description: "通过结构化追问访谈用户，澄清和精炼特性需求"
argument-hint: "[FT-XXX | 特性描述]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
EOF

# PRD 总体设计
cat > .claude/commands/qahc-harness-design-overall.md << 'EOF'
---
description: "基于需求澄清结果，生成 PRD 总体概述与使用场景"
argument-hint: "[FT-XXX]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md
EOF

# PRD 详细设计
cat > .claude/commands/qahc-harness-design-details.md << 'EOF'
---
description: "基于 PRD 总体设计进行 Code Research，补充技术方案、数据模型、验收条件"
argument-hint: "[FT-XXX]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md
EOF

# 开发任务拆解
cat > .claude/commands/qahc-harness-plan.md << 'EOF'
---
description: "基于 PRD 文档，拆解为可执行、可验证的开发任务清单"
argument-hint: "[FT-XXX]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md
EOF

# 开发实施
cat > .claude/commands/qahc-harness-develop.md << 'EOF'
---
description: "基于 Plan 文档按序执行开发任务，实现→验证→修复→确认闭环"
argument-hint: "[FT-XXX]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md
EOF

# 测试计划生成
cat > .claude/commands/qahc-harness-testplan.md << 'EOF'
---
description: "基于 PRD 文档生成结构化功能测试计划"
argument-hint: "[FT-XXX]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md
EOF

# 功能验收审查
cat > .claude/commands/qahc-harness-verify.md << 'EOF'
---
description: "对照 PRD 验收条件审查功能实现情况，生成验收报告"
argument-hint: "[FT-XXX]"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md
EOF
```

#### 对于 GitHub Copilot（`.github/prompts/`）：

GitHub Copilot 使用 `.prompt.md` 文件及 YAML 前置元数据：

```bash
mkdir -p .github/prompts/

# 工作区初始化
cat > .github/prompts/qahc-harness-init.prompt.md << 'EOF'
---
agent: 'agent'
description: '初始化驾驭工作区，创建目录骨架并生成项目总体计划'
argument-hint: ''
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md
EOF

# 需求访谈追问
cat > .github/prompts/qahc-harness-askme.prompt.md << 'EOF'
---
agent: 'agent'
description: '通过结构化追问访谈用户，澄清和精炼特性需求'
argument-hint: 'Enter FT-XXX or feature description'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
EOF

# PRD 总体设计
cat > .github/prompts/qahc-harness-design-overall.prompt.md << 'EOF'
---
agent: 'agent'
description: '基于需求澄清结果，生成 PRD 总体概述与使用场景'
argument-hint: 'Enter FT-XXX'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md
EOF

# PRD 详细设计
cat > .github/prompts/qahc-harness-design-details.prompt.md << 'EOF'
---
agent: 'agent'
description: '基于 PRD 总体设计进行 Code Research，补充技术方案、数据模型、验收条件'
argument-hint: 'Enter FT-XXX'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md
EOF

# 开发任务拆解
cat > .github/prompts/qahc-harness-plan.prompt.md << 'EOF'
---
agent: 'agent'
description: '基于 PRD 文档，拆解为可执行、可验证的开发任务清单'
argument-hint: 'Enter FT-XXX'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md
EOF

# 开发实施
cat > .github/prompts/qahc-harness-develop.prompt.md << 'EOF'
---
agent: 'agent'
description: '基于 Plan 文档按序执行开发任务，实现→验证→修复→确认闭环'
argument-hint: 'Enter FT-XXX'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md
EOF

# 测试计划生成
cat > .github/prompts/qahc-harness-testplan.prompt.md << 'EOF'
---
agent: 'agent'
description: '基于 PRD 文档生成结构化功能测试计划'
argument-hint: 'Enter FT-XXX'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md
EOF

# 功能验收审查
cat > .github/prompts/qahc-harness-verify.prompt.md << 'EOF'
---
agent: 'agent'
description: '对照 PRD 验收条件审查功能实现情况，生成验收报告'
argument-hint: 'Enter FT-XXX'
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md
EOF
```

#### 对于 Trae（`.trae/commands/`）：

Trae 使用 Markdown 命令文件及 YAML 前置元数据：

```bash
mkdir -p .trae/commands/

# 工作区初始化
cat > .trae/commands/qahc-harness-init.md << 'EOF'
---
name: qahc-harness-init
description: "初始化驾驭工作区，创建目录骨架并生成项目总体计划"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md
EOF

# 需求访谈追问
cat > .trae/commands/qahc-harness-askme.md << 'EOF'
---
name: qahc-harness-askme
description: "通过结构化追问访谈用户，澄清和精炼特性需求"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
EOF

# PRD 总体设计
cat > .trae/commands/qahc-harness-design-overall.md << 'EOF'
---
name: qahc-harness-design-overall
description: "基于需求澄清结果，生成 PRD 总体概述与使用场景"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md
EOF

# PRD 详细设计
cat > .trae/commands/qahc-harness-design-details.md << 'EOF'
---
name: qahc-harness-design-details
description: "基于 PRD 总体设计进行 Code Research，补充技术方案、数据模型、验收条件"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md
EOF

# 开发任务拆解
cat > .trae/commands/qahc-harness-plan.md << 'EOF'
---
name: qahc-harness-plan
description: "基于 PRD 文档，拆解为可执行、可验证的开发任务清单"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md
EOF

# 开发实施
cat > .trae/commands/qahc-harness-develop.md << 'EOF'
---
name: qahc-harness-develop
description: "基于 Plan 文档按序执行开发任务，实现→验证→修复→确认闭环"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md
EOF

# 测试计划生成
cat > .trae/commands/qahc-harness-testplan.md << 'EOF'
---
name: qahc-harness-testplan
description: "基于 PRD 文档生成结构化功能测试计划"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md
EOF

# 功能验收审查
cat > .trae/commands/qahc-harness-verify.md << 'EOF'
---
name: qahc-harness-verify
description: "对照 PRD 验收条件审查功能实现情况，生成验收报告"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md
EOF
```

#### 对于 Qoder（`.qoder/commands/`）：

Qoder 使用纯文本命令文件，文件名即命令名，内容为 prompt 正文，描述在 Qoder UI 中单独设置：

```bash
mkdir -p .qoder/commands/

# 工作区初始化
cat > .qoder/commands/qahc-harness-init.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md
EOF

# 需求访谈追问
cat > .qoder/commands/qahc-harness-askme.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
EOF

# PRD 总体设计
cat > .qoder/commands/qahc-harness-design-overall.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md
EOF

# PRD 详细设计
cat > .qoder/commands/qahc-harness-design-details.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md
EOF

# 开发任务拆解
cat > .qoder/commands/qahc-harness-plan.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md
EOF

# 开发实施
cat > .qoder/commands/qahc-harness-develop.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md
EOF

# 测试计划生成
cat > .qoder/commands/qahc-harness-testplan.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md
EOF

# 功能验收审查
cat > .qoder/commands/qahc-harness-verify.md << 'EOF'
follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md
EOF
```

> **注意**：Qoder 命令的描述（description）需在 Qoder UI 中单独设置，不在文件内容中。建议在 UI 中为每个命令添加对应的中文描述以便搜索识别。

#### 对于 OpenCode（`.opencode/commands/`）：

OpenCode 使用 Markdown 命令文件及 YAML 前置元数据，支持通过 `$ARGUMENTS` 传递参数：

```bash
mkdir -p .opencode/commands/

# 工作区初始化
cat > .opencode/commands/qahc-harness-init.md << 'EOF'
---
description: "初始化驾驭工作区，创建目录骨架并生成项目总体计划"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md
EOF

# 需求访谈追问
cat > .opencode/commands/qahc-harness-askme.md << 'EOF'
---
description: "通过结构化追问访谈用户，澄清和精炼特性需求"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
EOF

# PRD 总体设计
cat > .opencode/commands/qahc-harness-design-overall.md << 'EOF'
---
description: "基于需求澄清结果，生成 PRD 总体概述与使用场景"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md
EOF

# PRD 详细设计
cat > .opencode/commands/qahc-harness-design-details.md << 'EOF'
---
description: "基于 PRD 总体设计进行 Code Research，补充技术方案、数据模型、验收条件"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md
EOF

# 开发任务拆解
cat > .opencode/commands/qahc-harness-plan.md << 'EOF'
---
description: "基于 PRD 文档，拆解为可执行、可验证的开发任务清单"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md
EOF

# 开发实施
cat > .opencode/commands/qahc-harness-develop.md << 'EOF'
---
description: "基于 Plan 文档按序执行开发任务，实现→验证→修复→确认闭环"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md
EOF

# 测试计划生成
cat > .opencode/commands/qahc-harness-testplan.md << 'EOF'
---
description: "基于 PRD 文档生成结构化功能测试计划"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md
EOF

# 功能验收审查
cat > .opencode/commands/qahc-harness-verify.md << 'EOF'
---
description: "对照 PRD 验收条件审查功能实现情况，生成验收报告"
---

follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md
EOF
```

> **注意**：OpenCode 也支持在 `opencode.json` 中通过 `command` 键直接配置命令，格式等价。此外命令正文中可使用 `$ARGUMENTS` 占位符传递参数（如 `FT-XXX`）。

### 4. 安装所需 Skills

使用 `asdm` CLI 安装工具集所需的 skills：

```bash
asdm skill install context-loader
asdm skill install path-validator
asdm skill install markdownlint
```

这将安装以下 skills：
- **context-loader**：加载项目上下文和知识库，帮助 AI Agent 准确理解项目现状
- **path-validator**：验证文件路径和引用有效性，确保文档中的链接和引用准确无误
- **markdownlint**：对 Markdown 文档进行 lint 检查，保证文档格式规范和质量

### 5. 其他提供商的 Manual Usage

如果您的 AI 编码助手不在自动检测逻辑覆盖范围内（Claude Code、GitHub Copilot、Tencent CodeBuddy、Trae、Qoder、OpenCode），仍可手动使用本工具集：

#### 直接指令使用
您可以直接使用指令文件，将其相对路径复制粘贴到 AI 编码助手的聊天窗口：

1. **进入指令文件目录**：
   ```bash
   cd .asdm/toolsets/qahc-harness-toolset/actions/
   ```

2. **右键点击所需指令文件**并复制其相对路径：
   - 需求访谈：`qahc-harness-askme.md`
   - PRD 总体设计：`qahc-harness-design-overall.md`
   - PRD 详细设计：`qahc-harness-design-details.md`
   - 任务拆解：`qahc-harness-plan.md`
   - 开发实施：`qahc-harness-develop.md`
   - 测试计划：`qahc-harness-testplan.md`
   - 验收审查：`qahc-harness-verify.md`

3. **在 AI 编码助手中输入提示**：
   ```
   follow .asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md
   ```

## 工具集初始化

### 步骤〇：初始化工作区

安装完成后，首先初始化驾驭工作区：

```shell
/qahc-harness-init
```

这将：
- 创建工作区目录骨架（feat/、sessions/、reports/、cache/）
- 从项目上下文自动采集信息
- 生成项目总体计划（overall-plan.md）

### 步骤一：需求访谈

```shell
/qahc-harness-design-overall FT-XXX
```

这将：
- 基于 AskMe 文档生成 PRD 核心章节
- （阶段二）执行代码调研并补充详细设计
- 生成完整的 PRD 产品需求文档

### 步骤三：任务规划

```shell
/qahc-harness-plan FT-XXX
```

这将：
- 基于 PRD 拆解可执行任务
- 采用纵向拆分，每个任务端到端可验证
- 生成 Plan 实施计划文档

### 步骤五：编码实施

```shell
/qahc-harness-develop FT-XXX
```

这将：
- 按 Plan 任务顺序逐个执行
- 严格遵循 实现→验证→修复→人工确认 闭环
- 自动记录开发进度

### 步骤六：测试验证

```shell
/qahc-harness-testplan FT-XXX
```

这将：
- 基于 PRD 生成结构化测试计划
- 每个用例包含明确的测试步骤

### 步骤七：验收审查

```shell
/qahc-harness-verify FT-XXX
```

这将：
- 对照 PRD 验收条件逐项审查
- 生成功能验收报告

### 可用命令

安装完成后，可使用以下命令：

1. **`/qahc-harness-init`** — 工作区初始化：创建目录骨架并生成项目总体计划
2. **`/qahc-harness-askme`** — 需求访谈追问：通过结构化追问消除需求中的模糊点
3. **`/qahc-harness-design-overall`** — PRD 总体设计：生成总体概述与使用场景
4. **`/qahc-harness-design-details`** — PRD 详细设计：Code Research + 技术方案 + 验收条件
5. **`/qahc-harness-plan`** — 开发任务拆解：基于 PRD 拆解可执行开发计划
6. **`/qahc-harness-develop`** — 开发实施：按 Plan 启动编码闭环
7. **`/qahc-harness-testplan`** — 测试计划生成：生成功能测试套件与用例
8. **`/qahc-harness-verify`** — 功能验收审查：生成验收报告

## 工具集工作区结构

本工具集在 `.asdm/workspace/qahc-harness/` 下创建以下结构：

```
.asdm/workspace/qahc-harness/
├── overall-plan.md                        # 项目总体计划
└── feat/                                  # 特性工作目录
    └── FT-{id}-{name}/                    # 单个特性全流程文档
        ├── FT-{id}-{name}-AskMe.md        # 需求访谈文档
        ├── FT-{id}-{name}-PRD.md          # 产品需求文档
        ├── FT-{id}-{name}-CodeResearch-{module}.md  # 代码调研
        └── FT-{id}-{name}-Plan.md         # 开发任务计划
```

## 规范文档

本工具集使用以下规范文档作为模板：

1. **`overall-plan-spec.md`** — 总体计划模板，定义 overall-plan.md 的结构与填充规则
2. **`prd-spec.md`** — PRD 文档规范，定义产品需求文档的结构与质量标准
3. **`plan-spec.md`** — Plan 文档规范，定义开发计划的拆解粒度与内容要求
4. **`develop-spec.md`** — 编码规范，定义 AI 编码产出质量与开发流程状态管理
5. **`test-spec.md`** — 测试规范，定义测试计划结构与步骤规范
6. **`review-spec.md`** — 审查规范，定义验收报告与路径验证规则

## 验证

安装完成后，请验证：

1. `.asdm/workspace/qahc-harness/` 目录及其子目录已创建
2. QA Healthcare 驾驭工程工具集（toolset ID: `qahc-harness-toolset`）的快捷命令已创建在对应提供商目录中（如果使用 Claude Code、GitHub Copilot、Tencent CodeBuddy、Trae、Qoder 或 OpenCode）
3. 每个命令文件仅包含薄引用（`follow .asdm/toolsets/qahc-harness-toolset/actions/<action>.md`），而非复制内容
4. 本工具集文件位于 `.asdm/toolsets/qahc-harness-toolset`（toolset ID: `qahc-harness-toolset`）

**对于其他提供商**：请验证是否可以访问以下指令文件：
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-init.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-askme.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-overall.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-design-details.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-plan.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-develop.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-testplan.md`
- `.asdm/toolsets/qahc-harness-toolset/actions/qahc-harness-verify.md`

## 使用示例

### 完整开发流程

```shell
# 第〇步：初始化工作区
/qahc-harness-init

# 第一步：需求访谈，澄清需求
/qahc-harness-askme

# 第二步：基于澄清结果生成 PRD
/qahc-harness-design-overall FT-001

# 第三步：拆解开发任务
/qahc-harness-plan FT-001

# 第四步：逐个任务编码实施
/qahc-harness-develop FT-001

# 第五步：生成测试计划并验证
/qahc-harness-testplan FT-001

# 第六步：功能验收审查
/qahc-harness-verify FT-001
```

## 使用方式

### 对于受支持的提供商（Claude Code、GitHub Copilot、Tencent CodeBuddy、Trae、Qoder、OpenCode）
安装完成后，可直接使用以下快捷命令：
- `/qahc-harness-init`：工作区初始化
- `/qahc-harness-askme`：需求访谈追问
- `/qahc-harness-design-overall`：PRD 总体设计
- `/qahc-harness-design-details`：PRD 详细设计
- `/qahc-harness-plan`：开发任务拆解
- `/qahc-harness-develop`：开发实施
- `/qahc-harness-testplan`：测试计划生成
- `/qahc-harness-verify`：功能验收审查

### 对于其他提供商（Manual Usage）
如果您的提供商未被自动检测到，可按上面"其他提供商的 Manual Usage"中的步骤手动使用指令文件。

## 备注

- 本安装过程假设您拥有创建目录和文件的必要权限
- 命令文件使用**薄引用**（`follow` 指令），因此 action 文件的更新无需重新安装即可立即生效
- 使用本工具集前请确保 `qahc-context-builder` 工具集已安装并生成项目上下文
- 工具集 ID 在所有命令和文档中需保持一致

## 与其他工具集的集成

QA Healthcare 驾驭工程工具集可与以下 ASDM 工具集协同工作：
- **qahc-context-builder**：提供项目上下文知识库，确保 AI Agent 准确理解项目现状
- **toolset-builder**：如需扩展或定制本工具集的功能

### 获取帮助
如遇到问题，请参考：
- [ASDM 文档](https://asdm.ai/docs)
- 工具集 README：`.asdm/toolsets/qahc-harness-toolset/README.md`
- 规范文档在 `.asdm/toolsets/qahc-harness-toolset/specs/`

## 许可
Copyright (c) 2026 LeansoftX.com & iSoftStone. All rights reserved.

Licensed under the PROPRIETARY SOFTWARE LICENSE. See [LICENSE](LICENSE) in the project root for license information.

---

*本安装文档是 QA Healthcare 驾驭工程工具集的一部分。使用本工具集为 QA Healthcare 医疗问答系统项目提供完整的 AI 优先研发流水线。*
