> **⚠️ 模板声明**
>
> - 本文件是规范模板（Spec Template），非最终内容。
> - 执行 `/qahc-context-init` 时，AI 扫描项目后将 `[占位符]` 替换为真实信息，写入 `.asdm/contexts/index.md`。
> - 更新用 `/qahc-context-update`，验证用 `/qahc-context-validate`。

---

# [项目名称] — 工作区上下文索引

## 基本信息

- **名称**：[项目名称]
- **描述**：[一句话描述项目用途]
- **技术栈**：[语言] + [框架] + [构建工具]

## 目录结构

```
{根目录}/
├── .asdm/
│   ├── contexts/          # ★ 上下文文件
│   │   ├── index.md      # ★ 本文件（L1 入口）
│   │   └── layer-2/      # L2 详细上下文
│   └── toolsets/
├── server/                # 后端服务（按实际情况填充子目录）
├── web/                   # 前端应用（按实际情况填充子目录）
└── ...                    # 其他目录
```

## 服务与端口

| 服务 | 端口 | 说明 |
|------|------|------|
| [前端] | [端口] | [说明] |
| [后端服务A] | [端口] | [说明] |
| [后端服务B] | [端口] | [说明] |

## 开发命令

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动全部服务 |
| `npm run dev:web` | 仅启动前端 |
| `npm run build` | 构建生产版本 |

## L2 上下文导航

| 文件 | 说明 | 状态 |
|------|------|------|
| [standard-project-structure.md](./layer-2/standard-project-structure.md) | 项目结构详情 | 待生成 |
| [standard-coding-style.md](./layer-2/standard-coding-style.md) | 编码规范 | 待生成 |
| [data-models.md](./layer-2/data-models.md) | 数据模型 | 待生成 |
| [deployment.md](./layer-2/deployment.md) | 部署配置 | 待生成 |
| [api.md](./layer-2/api.md) | API 文档 | 待生成 |
| [architecture.md](./layer-2/architecture.md) | 架构设计 | 待生成 |

---

*由 QA Healthcare 上下文构建工具集维护。*
