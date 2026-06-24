# QAHC Action: API 接口测试

> **🔗 前置依赖**：本 action 在开发实施完成后执行（`/qahc-harness-develop` 全部任务完成），通过扫描源代码和过程文档提取所有 API 端点，使用 Postman + Newman 生成并执行接口测试。

## Metadata

```json
{
  "name": "qahc-harness-testapi",
  "displayName": "API接口测试",
  "description": "扫描源代码和过程文件提取所有API接口，使用Postman生成测试用例并通过Newman执行，自动分析报错根因并循环修复至全部通过",
  "toolset": {
    "id": "qahc-harness-toolset",
    "name": "QA Healthcare 驾驭工程工具集",
    "version": "0.0.1"
  },
  "scenario": "feature-testapi"
}
```

## Language Setting

默认使用**中文（简体中文）**作为输出语言。

## Context Injection

使用 [context-loader](../../../.asdm/skills/context-loader/SKILL.md) 技能进行渐进式上下文加载。

### 本 Action 的加载策略

| Phase | Context | 用途 |
|:-----:|---------|------|
| L1 必读 | `index.md` | 建立项目全局认知 |
| L2 按需 | `api.md` | 获取已知 API 端点定义 |
| L2 按需 | `architecture.md` | 了解服务架构和端口配置 |
| L2 按需 | `data-models.md` | 验证响应数据结构 |
| Source | PRD + CodeResearch + Plan | 从过程文档提取完整接口信息 |
| Source | 项目源代码 | 扫描 Controller 获取实际 API 端点 |
| Skill | Postman API Testing | 生成 Postman collection 及测试脚本 |
| Skill | markdownlint | 对最终测试报告进行 lint 检查 |

**IMPORTANT**: 在开始之前必须先读取 `../../../.asdm/contexts/index.md`。

## Description

在开发实施全部完成后，自动扫描项目源代码（后端 Controller）和过程文档（PRD、CodeResearch、Plan），提取所有 API 接口的完整信息（URL 路径、HTTP 方法、请求参数、响应格式、认证方式），使用 Postman API Testing 技能生成结构化的 Postman Collection 和 Environment 文件，通过 Newman CLI 执行接口测试，并对测试失败进行智能分析——区分「测试脚本错误」和「API 自身缺陷」，根据分析结果自动修复后重新运行，直至所有接口测试通过。

## Usage

```text
/qahc-harness-testapi [FT-XXX | 全部]
```

## Parameters

| 参数 | 必填 | 说明 |
|:------|:----:|------|
| `feature_id` | 否 | 特性编号 FT-XXX。留空则测试项目所有已实现的 API 接口 |

---

## Process

### 0. 环境准备与前置检查

#### 0.1 工作区初始化

1. 创建测试输出目录：

```bash
mkdir -p .asdm/workspace/qahc-harness/testapi/collections
mkdir -p .asdm/workspace/qahc-harness/testapi/environments
mkdir -p .asdm/workspace/qahc-harness/testapi/results
mkdir -p .asdm/workspace/qahc-harness/testapi/data
```

2. 检查 Newman 是否可用：

```bash
newman --version 2>/dev/null || npm install -g newman
```

#### 0.2 确定测试范围

- 若指定 `FT-XXX` → 仅测试该特性涉及的 API 接口
- 若未指定 → 测试项目所有已实现的 API 接口

---

### 1. API 接口提取

#### 1.1 从源代码扫描 API 端点

扫描所有后端服务的 Controller 类，提取接口信息：

1. **搜索 Controller 文件**：在后端服务目录下搜索所有包含 `@RestController` 或 `@Controller` 注解的 Java 文件
2. **解析每个 Controller**：提取以下信息
   - 类级别 `@RequestMapping` 路径前缀
   - 每个方法的 `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` 路径
   - HTTP 方法（GET / POST / PUT / DELETE）
   - 方法参数及其注解（`@RequestBody`、`@RequestParam`、`@PathVariable`）
   - 返回类型
3. **提取服务配置**：从 `application.properties` / `application.yml` 中提取服务端口

#### 1.2 从过程文档补充接口信息

若存在过程文档（PRD、CodeResearch），从中补充：

1. **PRD 文档**：从 `FT-{id}-{name}-PRD.md` 的接口设计章节提取
   - 接口的预期请求体格式
   - 预期响应格式和字段含义
   - 认证要求
2. **CodeResearch 文档**：从 `FT-{id}-{name}-CodeResearch-*.md` 提取
   - 技术实现细节
   - 异常情况处理

#### 1.3 汇总 API 清单

将扫描结果汇总为结构化清单：

```json
{
  "apis": [
    {
      "id": "api-001",
      "service": "qa-service-user",
      "port": 8080,
      "method": "GET",
      "path": "/api/test/cors",
      "full_url": "http://localhost:8080/api/test/cors",
      "description": "CORS测试接口",
      "request_params": [],
      "request_body": null,
      "expected_response": {
        "status": 200,
        "body_fields": ["message", "timestamp", "service"]
      },
      "auth_required": false,
      "source": "source_code"
    }
  ],
  "total_count": 1,
  "services": [
    {"name": "qa-service-user", "port": 8080},
    {"name": "qa-service-question", "port": 8081}
  ]
}
```

---

### 2. 生成 Postman 测试资源

加载 **Postman API Testing** 技能（`.codebuddy/skills/postman-api/SKILL.md`），按以下步骤生成测试资源：

#### 2.1 生成 Environment 文件

基于项目服务配置生成环境文件：

**输出路径**：`.asdm/workspace/qahc-harness/testapi/environments/local.postman_environment.json`

```json
{
  "name": "QA-Live-Healthcare-Local",
  "values": [
    { "key": "baseUrlUser", "value": "http://localhost:8080", "enabled": true },
    { "key": "baseUrlQuestion", "value": "http://localhost:8081", "enabled": true },
    { "key": "authToken", "value": "", "enabled": true },
    { "key": "testDataId", "value": "", "enabled": true },
    { "key": "responseTimeThreshold", "value": "3000", "enabled": true }
  ]
}
```

#### 2.2 生成 Collection 文件

为每个服务生成独立的 Postman Collection，按业务模块组织文件夹结构：

**Collection 组织规则**：
- 每个后端服务生成一个 Collection 文件
- Collection 内按 Controller 分组为 Folder
- 每个 API 端点对应一个 Request
- 每个 Request 必须包含 Test Scripts

**输出路径**：`.asdm/workspace/qahc-harness/testapi/collections/{service-name}.postman_collection.json`

#### 2.3 为每个请求编写 Test Scripts

每个 API 请求的 Test Scripts 必须覆盖以下验证维度：

1. **状态码验证**：验证 HTTP 状态码符合预期
2. **响应体结构验证**：验证 JSON 响应包含必需字段
3. **字段类型验证**：验证关键字段数据类型正确
4. **响应时间验证**：验证响应时间在可接受范围（默认 < 3000ms）
5. **Header 验证**：验证 Content-Type 为 `application/json`
6. **错误场景验证**（如适用）：验证错误响应格式

**Test Scripts 示例**（GET 请求）：

```javascript
pm.test('[状态码] 返回 200', () => {
    pm.response.to.have.status(200);
});

const jsonData = pm.response.json();

pm.test('[响应结构] 包含必需字段', () => {
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('timestamp');
    pm.expect(jsonData).to.have.property('service');
});

pm.test('[字段类型] 字段类型正确', () => {
    pm.expect(jsonData.message).to.be.a('string');
    pm.expect(jsonData.timestamp).to.be.a('string');
    pm.expect(jsonData.service).to.be.a('string');
});

pm.test('[Header] Content-Type 正确', () => {
    pm.response.to.have.header('Content-Type');
    pm.expect(pm.response.headers.get('Content-Type')).to.include('application/json');
});

pm.test('[性能] 响应时间 < 3000ms', () => {
    pm.expect(pm.response.responseTime).to.be.below(3000);
});
```

**Test Scripts 示例**（POST 请求）：

```javascript
pm.test('[状态码] 返回 200', () => {
    pm.response.to.have.status(200);
});

const jsonData = pm.response.json();

pm.test('[响应结构] 包含必需字段', () => {
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('receivedData');
});

pm.test('[数据回显] receivedData 与请求体一致', () => {
    const requestBody = JSON.parse(pm.request.body.raw);
    pm.expect(jsonData.receivedData).to.eql(requestBody);
});

pm.test('[Header] Content-Type 正确', () => {
    pm.response.to.have.header('Content-Type');
    pm.expect(pm.response.headers.get('Content-Type')).to.include('application/json');
});

pm.test('[性能] 响应时间 < 3000ms', () => {
    pm.expect(pm.response.responseTime).to.be.below(3000);
});
```

---

### 3. 执行接口测试

#### 3.1 启动服务（如需要）

检查目标服务是否已运行：

```bash
# 检查端口是否被占用
lsof -i :8080 2>/dev/null && echo "qa-service-user 已运行" || echo "qa-service-user 未运行，需要启动"
lsof -i :8081 2>/dev/null && echo "qa-service-question 已运行" || echo "qa-service-question 未运行，需要启动"
```

若服务未运行，启动对应服务：

```bash
# 启动 qa-service-user（Maven）
cd server/qa-service-user && mvn spring-boot:run -DskipTests &

# 启动 qa-service-question（Maven）
cd server/qa-service-question && mvn spring-boot:run -DskipTests &
```

等待服务启动（最多 30 秒）后验证健康状态。

#### 3.2 运行 Newman 测试

使用 Newman 运行生成的 Collection：

```bash
newman run .asdm/workspace/qahc-harness/testapi/collections/{service-name}.postman_collection.json \
  -e .asdm/workspace/qahc-harness/testapi/environments/local.postman_environment.json \
  -r cli,json \
  --reporter-json-export .asdm/workspace/qahc-harness/testapi/results/{service-name}-results.json \
  --timeout-request 10000 \
  --timeout-script 5000 \
  --bail
```

展示测试运行结果摘要：

```text
📊 {service-name} 接口测试结果

集合: {collection-name}
执行次数: N
├── ✅ 通过: N
├── ❌ 失败: N
└── ⏱️  平均响应时间: Nms

失败详情:
（如有）
```

---

### 4. 失败分析与修复循环

**最大重试次数：3 次**

```
第 N 次运行 → 有失败？
  ├── 否 → ✅ 测试通过，生成报告
  └── 是 → 分析失败原因 → 区分根因 → 修复 → 第 N+1 次运行
```

#### 4.1 失败根因分析

对每个失败的测试用例，按以下决策树分析根因：

```
测试断言失败
  ├── HTTP 状态码 ≠ 预期
  │     ├── 500 → 🔴 API 自身缺陷（服务端异常）
  │     ├── 404 → 🟡 检查路由配置（可能测试脚本 URL 错误）
  │     ├── 405 → 🟡 检查 HTTP 方法（可能测试脚本方法错误）
  │     ├── 401/403 → 🟡 检查认证配置（可能缺少 token）
  │     └── 4xx 其他 → 结合响应体分析
  ├── 响应体字段缺失/类型错误
  │     ├── 响应体为空且有 4xx/5xx → 🔴 API 自身缺陷
  │     ├── 响应体有内容但字段名不匹配 → 🟡 测试脚本预期字段需要修正
  │     └── 响应体字段类型与文档不符 → 🔴 API 返回数据格式问题
  ├── 响应时间超时
  │     └── 🟠 性能问题（记录但不阻塞，除非 > 10s）
  └── 连接失败 / DNS 错误
        └── 🟡 环境配置问题（服务未启动 / 端口错误）
```

**标记规则**：

| 标记 | 含义 | 修复方 |
|:----:|------|--------|
| 🔴 | API 自身缺陷 | 修改后端代码 |
| 🟡 | 测试脚本问题 | 修改 Postman 测试脚本 |
| 🟠 | 性能问题 | 记录在报告中 |

#### 4.2 修复实施

根据分析结果执行修复：

**API 自身缺陷（🔴）**：
1. 定位后端代码中出错的位置
2. 修复代码逻辑
3. 重新编译并重启服务
4. 更新 `develop-log.json` 记录修复动作

**测试脚本问题（🟡）**：
1. 对比实际 API 响应与测试脚本预期
2. 修正 Postman Collection 中对应的 Test Scripts
3. 导出更新后的 Collection JSON

#### 4.3 重新运行

修复完成后重新执行 Newman 测试，进入下一轮验证。若达到最大重试次数（3次）仍有失败：

```text
⛔ API 接口测试阻塞：连续 3 次修复后仍存在失败接口

失败接口：
- GET /api/xxx → 状态码 500，根因：{分析结论}

建议：人工介入检查 {具体模块} 的代码逻辑。
阻塞详情已记录在 develop-log.json 和测试报告中。
```

---

### 5. 生成测试报告

所有接口测试通过后，生成测试报告：

**输出路径**：`.asdm/workspace/qahc-harness/testapi/results/api-test-report-{date}.md`

**报告结构**：

```markdown
# API 接口测试报告

## 测试概览

| 项目 | 值 |
|------|-----|
| 测试时间 | {YYYY-MM-DD HH:mm:ss} |
| 测试范围 | {全部 / FT-XXX} |
| 服务数量 | N |
| 接口总数 | N |
| 测试用例总数 | N |
| 通过率 | 100% |
| 修复轮次 | N |

## 接口清单

| # | 服务 | 方法 | 路径 | 测试用例数 | 状态 |
|---|------|------|------|:----------:|:----:|
| 1 | qa-service-user | GET | /api/xxx | 5 | ✅ |
| ... | ... | ... | ... | ... | ... |

## 各服务测试详情

### qa-service-user (端口 8080)

#### GET /api/test/cors

**请求示例**：
`GET http://localhost:8080/api/test/cors`

**响应示例**：
{...}

**测试结果**：
| 测试用例 | 状态 | 响应时间 |
|----------|:----:|----------|
| 状态码验证 | ✅ | - |
| 响应体结构验证 | ✅ | - |
| 字段类型验证 | ✅ | - |
| Header 验证 | ✅ | - |
| 性能验证 | ✅ | 15ms |

## 修复记录

（如有修复操作，记录每轮修复的内容）

## 测试资源

| 资源类型 | 路径 |
|----------|------|
| Collection | .asdm/workspace/qahc-harness/testapi/collections/ |
| Environment | .asdm/workspace/qahc-harness/testapi/environments/local.postman_environment.json |
| Results JSON | .asdm/workspace/qahc-harness/testapi/results/ |
```

#### 5.1 对报告进行 Lint 检查

使用 markdownlint 技能对生成的测试报告进行格式校验：

1. 加载 markdownlint 技能（`.asdm/skills/markdownlint/SKILL.md`）
2. 对报告文件执行 lint 检查
3. 如有格式问题，自动修复后重新写入

---

### 6. 完成提示

```text
✅ API 接口测试全部通过！

📊 测试概览：
- 测试服务: N 个
- 接口总数: N 个
- 测试用例: N 个
- 通过率: 100%
- 修复轮次: N 次

📄 测试报告: .asdm/workspace/qahc-harness/testapi/results/api-test-report-{date}.md
📁 Postman Collection: .asdm/workspace/qahc-harness/testapi/collections/
📁 Postman Environment: .asdm/workspace/qahc-harness/testapi/environments/

📌 后续可执行 /qahc-harness-verify 进行功能验收审查。
```

---

## Purpose

- 在开发实施完成后，通过自动化方式验证所有 API 接口的正确性
- 利用 Postman + Newman 实现可重复、可版本控制的接口测试
- 智能区分测试脚本错误和 API 代码缺陷，精准定位问题
- 通过自动修复→重新测试循环确保接口质量

## Input

- 特性编码 FT-XXX（可选，不指定则测试全部）
- 后端源代码（扫描 Controller 提取 API）
- 过程文档（PRD、CodeResearch、Plan）
- 项目上下文（L1/L2 context）
- Postman API Testing 技能

## Output

```json
{
  "phase": "api_testing",
  "status": "success | failed | blocked",
  "test_rounds": 1,
  "summary": {
    "services_tested": 2,
    "total_apis": 5,
    "total_test_cases": 25,
    "passed": 25,
    "failed": 0,
    "pass_rate": "100%"
  },
  "fixes_applied": {
    "api_fixes": 0,
    "script_fixes": 0
  },
  "report_path": ".asdm/workspace/qahc-harness/testapi/results/api-test-report-{date}.md",
  "collection_paths": [
    ".asdm/workspace/qahc-harness/testapi/collections/qa-service-user.postman_collection.json",
    ".asdm/workspace/qahc-harness/testapi/collections/qa-service-question.postman_collection.json"
  ],
  "timestamp": "ISO 8601 datetime"
}
```

## Downstream Flow

```text
/qahc-harness-develop          ← 上一步：开发实施（全部任务完成）
        │
        ▼
/qahc-harness-testapi           ← 当前步骤：API 接口测试
        │
        ▼
/qahc-harness-verify            ← 下一步：功能验收审查
```

## Spec & Skill Reference

- [Postman API Testing Skill](../../../.codebuddy/skills/postman-api/SKILL.md) — Postman 接口测试技能
- [markdownlint Skill](../../../.asdm/skills/markdownlint/SKILL.md) — Markdown 格式校验
