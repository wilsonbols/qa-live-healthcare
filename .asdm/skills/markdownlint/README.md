# Markdown Lint Skill

Markdown 文件质量保障技能，基于 [markdownlint-cli](https://github.com/igorubin/markdownlint-cli) 封装。

## 功能

- **语法验证**：检查 Markdown 文件的语法正确性
- **自动修复**：一键修复可自动处理的格式问题
- **规则配置**：支持自定义 lint 规则集
- **Mermaid 支持**：针对 Mermaid 图表的专项检查建议
- **批量验证**：目录级递归检查

## 快速开始

```bash
# 安装依赖
npm install -g markdownlint-cli

# 验证单个文件
markdownlint your-file.md

# 自动修复
markdownlint --fix your-file.md

# 使用项目配置
markdownlint -c .markdownlint.json ./docs/
```

## 文件结构

```
.asdm/skills/markdownlint/
├── manifest.json      # 技能元数据
├── SKILL.md           # 核心指令（AI 读取此文件了解如何使用）
└── README.md          # 本文件 — 用户说明文档
```

## 适用场景

| 场景 | 说明 |
|------|------|
| AI 生成 .md 后 | 确保输出语法正确、格式规范 |
| 文档审查 | 批量检查文档质量 |
| CI/CD 集成 | 在构建流程中加入质量门禁 |
| 团队规范统一 | 通过 `.markdownlint.json` 统一风格 |

## 规则参考

完整规则列表：https://github.com/DavidAnson/markdownlint/blob/main/doc/Rules.md

## License

Proprietary. See project root [LICENSE](LICENSE) for terms.
