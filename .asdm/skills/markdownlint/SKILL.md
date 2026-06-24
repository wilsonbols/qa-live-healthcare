---
name: markdownlint
description: 'Markdown validation, linting, and quality assurance. Use this skill whenever AI generates or edits Markdown files to ensure they follow proper syntax, formatting standards, and best practices. Triggers for: (1) Validating .md files before output, (2) Auto-fixing common Markdown issues, (3) Enforcing consistent style across documentation, (4) Checking Mermaid diagram syntax in .md files'
license: Proprietary. See LICENSE.txt for complete terms
---

# Markdown Lint Skill — Markdown 文件质量保障

## Overview

本技能封装 `markdownlint-cli` 工具，为 AI 代理提供标准化的 Markdown 语法检查、自动修复和质量保障能力。当 AI 生成或编辑 `.md` 文件时，应使用此技能确保输出文件的语法正确性和风格一致性。

## Prerequisites（前置条件）

### 检查 markdownlint-cli 是否已安装

```bash
# 检查是否已安装
which markdownlint && markdownlint --version
```

如果未安装：

```bash
npm install -g markdownlint-cli
```

## 文件生成边界

**被 action（如 qahc-context-init / qahc-context-update）调用时，验证过程严禁在工作区根目录或非目标路径创建任何文件。**

| 允许的操作 | 禁止的操作 |
|-----------|-----------|
| 使用 `-r` 内联规则覆盖参数 | 创建 `.markdownlint.json` / `.markdownlint.yaml` 等配置文件 |
| 使用项目已有配置（`-c <已有配置>`） | 在工作区根目录写入任何新文件 |
| 直接编辑目标 `.md` 文件修复问题 | 生成验证脚本、报告文件等额外产物 |

正确做法示例：

```bash
# ✅ 使用内联规则适配中文文档（不创建任何文件）
markdownlint -r ~MD013,~MD033 <file-path.md>

# ✅ 项目已有配置时使用 -c 指定
markdownlint -c .markdownlint.json <file-path.md>

# ❌ 禁止：为通过验证而创建新配置文件
```

## 核心工作流

### 工作流 A：生成/编辑后验证（推荐）

在每次 AI 生成或修改 `.md` 文件后执行验证：

```bash
# 基础验证（使用内联规则适配中文文档）
markdownlint -r ~MD013,~MD024,~MD033,~MD049 <file-path.md>

# 验证整个目录
markdownlint -r ~MD013,~MD024,~MD033,~MD049 <directory-path>/
```

> **提示**：中文文档建议默认忽略 `MD013`（行长度）、放宽 `MD033`（内联 HTML）。
> 通过 `-r` 参数一次性传入，无需创建配置文件。

**判断结果**：
- **Exit code 0**：✅ 无问题
- **Exit code 1**：⚠️ 发现问题（非致命），需要查看输出并修复

### 工作流 B：验证 + 自动修复

对于可自动修复的问题（如尾部空格、空白行不一致等）：

```bash
# 实际修复（直接修改目标 .md 文件，不产生额外文件）
markdownlint -r ~MD013,~MD033 --fix <file-path.md>
```

> **注意**：`--fix` 只能修复部分规则。需要人工判断的问题（如行长度、内容逻辑错误）仍需手动处理。

## 常用命令速查

| 操作 | 命令 |
|------|------|
| **验证单个文件** | `markdownlint file.md` |
| **验证目录下所有 .md** | `markdownlint ./docs/` |
| **使用配置文件** | `markdownlint -c .markdownlint.json file.md` |
| **仅显示错误摘要** | `markdownlint -s file.md` |
| **JSON 格式输出** | `markdownlint --json file.md` |
| **忽略特定规则** | `markdownlint -r ~MD013,~MD040 file.md` |
| **自动修复** | `markdownlint --fix file.md` |
| **指定规则集** | `markdownlint --config rules.json file.md` |

## 规则说明（常用）

### 必须关注的规则（影响渲染正确性）

| 规则码 | 名称 | 说明 | 严重程度 | 可自动修复 |
|--------|------|------|---------|-----------|
| MD022 | `blanks-around-headings` | 标题前后需有空行 | 高 | ✅ |
| MD032 | `blanks-around-lists` | 列表前后需有空行 | 中 | ✅ |
| MD040 | `fenced-code-language` | 代码块必须标注语言 | **高** | ❌ |
| MD047 | `single-trailing-newline` | 文件末尾需有换行符 | 中 | ✅ |
| **MD060** | `table-column-style` | 表格管道符格式一致性 | **高** | ✅ |

### 推荐关注的质量规则

| 规则码 | 名称 | 说明 | 建议 |
|--------|------|------|------|
| MD013 | `line-length` | 行长度限制 | 中文文档建议放宽至 120 或禁用 |
| MD014 | `commands-show-output` | 命令应展示输出示例 | 建议开启 |
| MD024 | `no-duplicate-heading` | 不允许重复标题 | 建议开启 |
| MD029 | `ol-prefix` | 有序列表前缀风格 | 保持默认 |
| MD033 | `no-inline-html` | 禁止内联 HTML | 技术文档常需放宽 |
| MD036 | `emphasis-as-header` | 不要用强调代替标题 | 建议开启 |

### Mermaid 图表专项检查

Markdown 本身的 linter 无法深度解析 Mermaid 语法，但以下相关规则有助于发现常见问题：

1. **MD040**：确认代码块标注了 `mermaid` 语言（否则不会渲染）
2. **MD013 / 行长度**：Mermaid 图表中的长节点定义容易超长，注意换行
3. **MD031**：代码块周围需有空行（影响某些渲染器）
4. **人工检查项**（linter 覆盖不到）：
   - 特殊字符转义：`>` `<` `&` 在节点文本中可能需要引号包裹
   - subgraph ID 不能包含中文或特殊字符
   - 边标签中的 `>` 是 Mermaid 保留字，需避免裸用

## 配置文件

> **⚠️ 重要**：配置文件仅作为参考模板展示。当被 action 调用时，
> **禁止**为通过验证而创建新的配置文件到工作区。应使用 `-r` 内联参数替代。

### 项目级配置 `.markdownlint.json`（参考模板）

如团队已有此文件可直接使用；若无，不应由 action 自动创建：

```json
{
  "default": true,
  "MD013": {
    "line_length": 120,
    "code_blocks": false,
    "tables": false,
    "headings": false
  },
  "MD033": {
    "allowed_elements": ["br", "details", "summary", "sup", "sub"]
  },
  "MD060": {
    "style": "compact"
  },
  "MD049": {
    "style": "consistent"
  }
}
```

### ASDM 项目推荐配置

适用于 ASDM toolset / specs / action 等 Markdown 文件的严格配置：

```json
{
  "default": true,
  "MD013": false,
  "MD024": { "siblings_only": true },
  "MD033": false,
  "MD060": { "style": "padded" },
  "MD049": false
}
```

## 与 Action 集成

本 SKILL 应被以下类型的 action 引用。**调用时必须遵守「文件生成边界」约束，不得创建配置文件或额外产物。**

### 在 action 末尾添加质量检查步骤

任何生成 `.md` 输出文件的 action，都应在「输出」阶段之后增加验证步骤：

```
## 输出质量保证

1. 执行 `/skill markdownlint` 对生成的所有 .md 文件进行语法验证
2. 如发现问题：
   a. 能自动修复的 → 运行 `markdownlint --fix <file>`
   b. 需要人工判断的 → 向用户报告问题并建议修复方案
3. 仅当 exit code 为 0 或所有问题已处理后，视为任务完成
```

### 验证范围矩阵

| Action 类型 | 验证范围 | 时机 |
|-------------|---------|------|
| **init（初始化）** | 所有新生成的 L1 + L2 文件 | 全部生成完毕后 |
| **update（更新）** | 本次修改的 L2 文件 | 每个文件更新完成后 |
| **validate（验证）** | 被验证的上下文文件 + 自身报告 | 报告生成后 |

## 输出解读与修复指南

### 错误格式

```
<file-path>:<line>:<column> <error/warning> <rule-id>/<rule-name> <message>
```

### 常见问题快速修复

| 问题 | 示例 | 修复方式 |
|------|------|---------|
| 代码块无语言标注 | `MD040: Fenced code blocks should have a language specified` | 在 \`\`\` 后加语言名，如 \`\`\`bash |
| 表格格式不一致 | `MD060: Table pipe is missing space` | 确保 `\|` 两边有空格（padded 风格） |
| 标题缺少空行 | `MD022: Headings should be surrounded by blank lines` | 在标题上下各加一行空行 |
| 行过长 | `MD013: Line length [Expected: 80; Actual: 167]` | 放宽限制或在配置中禁用 |
| 尾部多余空格 | `MD009: Trailing spaces` | 运行 `--fix` 自动删除 |
| 文件末尾缺换行 | `MD047: Files should end with a single newline` | 运行 `--fix` 自动添加 |
| 内联 HTML 未允许 | `MD033: Inline HTML` | 在配置中添加 allowed_elements 或忽略此规则 |

## 完整验证脚本（可选）

对于批量验证场景，可以使用以下 shell 脚本：

```bash
#!/bin/bash
# validate-markdown.sh — 批量验证指定路径下的 .md 文件
# 用法: ./validate-markdown.sh <path>

TARGET="${1:-.}"
echo "🔍 Validating Markdown files in: $TARGET"
echo "──────────────────────────────────────"

find "$TARGET" -name "*.md" -not -path "*/node_modules/*" | while read -r f; do
  RESULT=$(markdownlint -s "$f" 2>&1)
  if [ $? -eq 0 ]; then
    echo "  ✅ $f"
  else
    echo "  ⚠️  $f"
    echo "$RESULT" | sed 's/^/     /'
  fi
done

echo "──────────────────────────────────────"
echo "Validation complete."
```
