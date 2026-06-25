# QAHC Action: API 接口测试（JMeter）

> **🔗 前置依赖**：本 action 在开发实施完成后执行（`/qahc-harness-develop` 全部任务完成），通过扫描源代码和过程文档提取所有 API 端点，使用 Apache JMeter 生成并执行接口自动化测试。

## Metadata

```json
{
  "name": "qahc-harness-testapi",
  "displayName": "API接口测试",
  "description": "扫描源代码和过程文件提取所有API接口，使用Apache JMeter生成测试计划并通过CLI执行，自动分析报错根因并循环修复至全部通过",
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
| Skill | markdownlint | 对最终测试报告进行 lint 检查 |

**IMPORTANT**: 在开始之前必须先读取 `../../../.asdm/contexts/index.md`。

## Description

在开发实施全部完成后，自动扫描项目源代码（后端 Controller）和过程文档（PRD、CodeResearch、Plan），提取所有 API 接口的完整信息（URL 路径、HTTP 方法、请求参数、响应格式、认证方式），使用 Apache JMeter 生成结构化的测试计划文件（.jmx），通过 JMeter CLI（非 GUI 模式）执行接口测试，并对测试失败进行智能分析——区分「测试脚本错误」和「API 自身缺陷」，根据分析结果自动修复后重新运行，直至所有接口测试通过。

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
mkdir -p .asdm/workspace/qahc-harness/testapi/testplans
mkdir -p .asdm/workspace/qahc-harness/testapi/data
mkdir -p .asdm/workspace/qahc-harness/testapi/results
mkdir -p .asdm/workspace/qahc-harness/testapi/reports
```

2. 检查 `JMETER_HOME` 环境变量是否已设置，并根据操作系统调用对应的 JMeter 工具：

```powershell
# Windows (PowerShell)
if (-not $env:JMETER_HOME) {
    Write-Host "❌ JMETER_HOME 环境变量未设置" -ForegroundColor Red
    Write-Host ""
    Write-Host "JMeter 安装指引：" -ForegroundColor Yellow
    Write-Host "  1. 下载 Apache JMeter 5.5+：https://jmeter.apache.org/download_jmeter.cgi"
    Write-Host "  2. 解压到本地目录（如 C:\apache-jmeter-5.6.3）"
    Write-Host "  3. 设置环境变量 JMETER_HOME 指向解压目录"
    Write-Host "  4. 将 %JMETER_HOME%\bin 加入 PATH 环境变量"
    exit 1
}
$jmeterCmd = "$env:JMETER_HOME\bin\jmeter.bat"
if (-not (Test-Path $jmeterCmd)) {
    Write-Host "❌ JMETER_HOME 指向的路径中未找到 jmeter.bat：$jmeterCmd" -ForegroundColor Red
    exit 1
}
& $jmeterCmd --version
```

```bash
# Linux/macOS
if [ -z "$JMETER_HOME" ]; then
    echo "❌ JMETER_HOME 环境变量未设置"
    echo ""
    echo "JMeter 安装指引："
    echo "  1. 下载 Apache JMeter 5.5+：https://jmeter.apache.org/download_jmeter.cgi"
    echo "  2. 解压到本地目录（如 /opt/apache-jmeter-5.6.3）"
    echo "  3. 设置环境变量 export JMETER_HOME=/opt/apache-jmeter-5.6.3"
    echo "  4. 将 \$JMETER_HOME/bin 加入 PATH"
    exit 1
fi
if [ ! -f "$JMETER_HOME/bin/jmeter" ]; then
    echo "❌ JMETER_HOME 指向的路径中未找到 jmeter：$JMETER_HOME/bin/jmeter"
    exit 1
fi
$JMETER_HOME/bin/jmeter --version
```

> **IMPORTANT**：本 action 不会尝试安装 JMeter。若 `JMETER_HOME` 环境变量未设置或无效，立即终止对话并提示用户安装配置 JMeter。

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

### 2. 生成 JMeter 测试计划

按以下步骤生成 JMeter 测试资源。

#### 2.0 测试数据加载策略（方式 2：独立 Sampler + CSV 局部参数化）

每个 `.jmx` 测试计划文件采用 **独立 Sampler + CSV 局部参数化** 方式组织：

```
Thread Group
  ├── CSV Data Set Config（Thread Group 级别）
  │     └── 提供: testCase, dummy1, dummy2, dummy3, expectedStatus, expectedContentType
  │     └── recycle=false, stopThread=true → 每个 Sampler 顺序读取一行
  │
  ├── Sampler 1: GET /api/xxx     ← method/path/body 硬编码
  ├── Sampler 2: POST /api/xxx    ← method/path/body 硬编码
  ├── Sampler 3: DELETE /api/xxx  ← method/path 硬编码
  └── ...
```

**设计原则**：

| 维度 | 存放位置 | 原因 |
|------|----------|------|
| `method` / `path` | 每个 Sampler 内硬编码 | 避免 CSV 变量与 Sampler 固有属性冲突，防止 CSV 行切换时值错乱 |
| `requestBody`（JSON） | POST/PUT Sampler 内硬编码 | CSV 中转义引号会导致 JSON 解析异常 |
| `expectedStatus` | 从 CSV `${expectedStatus}` 读取 | 真正的可参数化数据，便于扩展正常/异常/边界场景 |
| `expectedContentType` | 从 CSV `${expectedContentType}` 读取 | 部分接口（如 OPTIONS）无 JSON 响应体 |
| 不用的 CSV 列 | `dummy1/dummy2/...` 占位 | 保持 CSV 列数一致，同时避免变量覆盖 Sampler 内部值 |

#### 2.1 生成测试数据文件（CSV Data Set）

首先根据 API 清单生成 CSV 数据文件（每个服务一个），此文件既是测试数据，也是接口文档索引：

**输出路径**：`.asdm/workspace/qahc-harness/testapi/data/{service-name}-test-data.csv`

**CSV 格式说明**：

| 列 | 含义 | 示例 | 是否驱动 Sampler |
|----|------|------|:----:|
| `testCase` | 用例名称 | `GET CORS测试` | 文档标记 |
| `method` | HTTP 方法 | `GET` / `POST` | ❌ dummy占位 |
| `path` | 请求路径 | `/api/test/cors` | ❌ dummy占位 |
| `requestBody` | 请求体（含转义） | `"{""name"":""test""}"` | ❌ dummy占位 |
| `expectedStatus` | 预期状态码 | `200` / `400` | ✅ 驱动断言 |
| `expectedContentType` | 预期 Content-Type | `application/json` | 文档标记 |

**CSV 示例**：

```csv
testCase,method,path,requestBody,expectedStatus,expectedContentType
GET CORS测试,GET,/api/test/cors,,200,application/json
POST CORS测试,POST,/api/test/cors,"{""name"":""test"",""value"":123}",200,application/json
POST 空Body测试,POST,/api/test/cors,,200,application/json
OPTIONS CORS预检,OPTIONS,/api/test/cors,,200,
```

> **IMPORTANT**：CSV 中 `method`、`path`、`requestBody` 列仅作文档参考，**不参与实际测试执行**。实际请求的方法/路径/请求体由每个 `HTTP Request` Sampler 内部硬编码决定。

#### 2.2 生成 JMeter 测试计划（.jmx）

为每个服务生成一个 `.jmx` 测试计划文件。

**测试计划组织规则**：

- 每个后端服务生成一个 `.jmx` 测试计划文件
- 采用 `Test Plan` → `Thread Group` → `CSV Data Set Config` → `HTTP Sampler` 层级结构
- 每个 Thread Group 对应一个 Controller
- Thread Group 级别放置一个 `CSV Data Set Config` 加载外部 CSV 数据
- 每个 API 端点在 Thread Group 内生成一个独立的 `HTTP Request` Sampler（`method`/`path`/`requestBody` 硬编码）
- 每个 Sampler 包含独立的断言集（断言中 `expectedStatus` 通过 `${expectedStatus}` 从 CSV 读取）

**输出路径**：`.asdm/workspace/qahc-harness/testapi/testplans/{service-name}-test-plan.jmx`

**CSV Data Set Config 与 Thread Group 的对应关系**：

- CSV 行数 = Thread Group 内 Sampler 数量
- `recycle=false`, `stopThread=true` → 每个 Sampler 顺序消耗 CSV 一行
- 变量名中不用的列使用 `dummyN` 占位，避免变量名冲突

**.jmx 模板结构**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="{service-name} API Test Plan" enabled="true">
      <stringProp name="TestPlan.comments">自动生成的 API 接口测试计划</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments">
          <elementProp name="host" elementType="Argument">
            <stringProp name="Argument.name">host</stringProp>
            <stringProp name="Argument.value">localhost</stringProp>
          </elementProp>
          <elementProp name="port" elementType="Argument">
            <stringProp name="Argument.name">port</stringProp>
            <stringProp name="Argument.value">{服务端口}</stringProp>
          </elementProp>
          <elementProp name="protocol" elementType="Argument">
            <stringProp name="Argument.name">protocol</stringProp>
            <stringProp name="Argument.value">http</stringProp>
          </elementProp>
          <elementProp name="responseTimeThreshold" elementType="Argument">
            <stringProp name="Argument.name">responseTimeThreshold</stringProp>
            <stringProp name="Argument.value">3000</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="{Controller名称}" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">1</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">1</stringProp>
        <stringProp name="ThreadGroup.ramp_time">1</stringProp>
      </ThreadGroup>
      <hashTree>
        <!-- ===== CSV Data Set Config（加载外部测试数据） ===== -->
        <CSVDataSet guiclass="TestBeanGUI" testclass="CSVDataSet" testname="测试数据 (CSV Data Set)" enabled="true">
          <stringProp name="delimiter">,</stringProp>
          <stringProp name="fileEncoding">UTF-8</stringProp>
          <stringProp name="filename">.asdm/workspace/qahc-harness/testapi/data/{service-name}-test-data.csv</stringProp>
          <boolProp name="ignoreFirstLine">true</boolProp>
          <boolProp name="quotedData">false</boolProp>
          <boolProp name="recycle">false</boolProp>
          <boolProp name="stopThread">true</boolProp>
          <stringProp name="variableNames">testCase,dummyMethod,dummyPath,dummyBody,expectedStatus,expectedContentType</stringProp>
        </CSVDataSet>
        <hashTree/>

        <!-- ===== 以下每个 Sampler method/path/body 硬编码 ===== -->

        <!-- Sampler: GET {path} -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="GET {path}" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments"/>
          </elementProp>
          <stringProp name="HTTPSampler.domain">${host}</stringProp>
          <stringProp name="HTTPSampler.port">${port}</stringProp>
          <stringProp name="HTTPSampler.protocol">${protocol}</stringProp>
          <stringProp name="HTTPSampler.path">{path}</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.implementation">HttpClient4</stringProp>
          <boolProp name="HTTPSampler.postBodyRaw">false</boolProp>
        </HTTPSamplerProxy>
        <hashTree>
          <!-- 状态码断言（从CSV读取） -->
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="状态码断言" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">${expectedStatus}</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">8</intProp>
          </ResponseAssertion>
          <hashTree/>
          <!-- JSR223 断言 - 响应结构验证（Groovy脚本根据接口自定义） -->
          <JSR223Assertion guiclass="TestBeanGUI" testclass="JSR223Assertion" testname="响应结构验证" enabled="true">
            <stringProp name="scriptLanguage">groovy</stringProp>
            <stringProp name="parameters"></stringProp>
            <stringProp name="filename"></stringProp>
            <stringProp name="cacheKey">true</stringProp>
            <stringProp name="script">{Groovy 断言脚本}</stringProp>
          </JSR223Assertion>
          <hashTree/>
          <!-- 响应时间断言 -->
          <DurationAssertion guiclass="DurationAssertionGui" testclass="DurationAssertion" testname="响应时间 &lt; ${responseTimeThreshold}ms" enabled="true">
            <stringProp name="DurationAssertion.duration">${responseTimeThreshold}</stringProp>
          </DurationAssertion>
          <hashTree/>
          <!-- Content-Type Header 断言 -->
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Content-Type 包含 application/json" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="1314979797">application/json</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_headers</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">2</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>

        <!-- Sampler: POST {path}（带 Body） -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="POST {path} (带请求体)" enabled="true">
          <stringProp name="HTTPSampler.domain">${host}</stringProp>
          <stringProp name="HTTPSampler.port">${port}</stringProp>
          <stringProp name="HTTPSampler.protocol">${protocol}</stringProp>
          <stringProp name="HTTPSampler.path">{path}</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.implementation">HttpClient4</stringProp>
          <boolProp name="HTTPSampler.postBodyRaw">true</boolProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{JSON请求体}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <stringProp name="Argument.name"></stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
        </HTTPSamplerProxy>
        <hashTree>
          <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager" enabled="true">
            <collectionProp name="HeaderManager.headers">
              <elementProp name="" elementType="Header">
                <stringProp name="Header.name">Content-Type</stringProp>
                <stringProp name="Header.value">application/json</stringProp>
              </elementProp>
            </collectionProp>
          </HeaderManager>
          <hashTree/>
          <!-- 状态码断言（从CSV读取） -->
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="状态码断言" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">${expectedStatus}</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">8</intProp>
          </ResponseAssertion>
          <hashTree/>
          <!-- JSR223 断言 - POST 响应验证（Groovy脚本根据接口自定义） -->
          <JSR223Assertion guiclass="TestBeanGUI" testclass="JSR223Assertion" testname="POST响应结构验证" enabled="true">
            <stringProp name="scriptLanguage">groovy</stringProp>
            <stringProp name="parameters"></stringProp>
            <stringProp name="filename"></stringProp>
            <stringProp name="cacheKey">true</stringProp>
            <stringProp name="script">{Groovy 断言脚本}</stringProp>
          </JSR223Assertion>
          <hashTree/>
          <DurationAssertion guiclass="DurationAssertionGui" testclass="DurationAssertion" testname="响应时间 &lt; ${responseTimeThreshold}ms" enabled="true">
            <stringProp name="DurationAssertion.duration">${responseTimeThreshold}</stringProp>
          </DurationAssertion>
          <hashTree/>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Content-Type 包含 application/json" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="1314979797">application/json</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_headers</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">2</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
        <!-- ... 更多 Sampler 按此模式追加 ... -->
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**生成规则**：

1. **User Defined Variables**：拆分为 `host`、`port`、`protocol` 三个独立变量（不要用完整 URL），`{服务端口}` 填入从 `application.properties` 提取的实际端口号
2. **CSV Data Set Config**：`variableNames` 中不用的列用 `dummyMethod`/`dummyPath`/`dummyBody` 占位，只用 `expectedStatus` 和 `expectedContentType`
3. **每个 API 端点生成一个独立的 Sampler**，`method`/`path`/`requestBody` 全部硬编码，不引用 CSV 变量
4. **每个 Sampler 的状态码断言**统一使用 `${expectedStatus}` 从 CSV 读取
5. **POST/PUT Sampler** 必须包含 `HeaderManager`（`Content-Type: application/json`），且 `postBodyRaw=true`

#### 2.3 断言配置规范

每个 API 请求必须覆盖以下断言维度：

| 断言类型 | JMeter 组件 | 配置要点 |
|---------|------------|---------|
| 状态码断言 | `Response Assertion` | `test_type=8`(Equals)，值用 `${expectedStatus}` 从 CSV 读取 |
| 响应体结构断言 | `JSR223 Assertion` (Groovy) | JSON 响应包含必需字段，具体字段根据 API 实际响应定义 |
| 字段类型断言 | `JSR223 Assertion` (Groovy) | 关键字段数据类型正确 |
| 响应时间断言 | `Duration Assertion` | 默认 < 3000ms |
| Header 断言 | `Response Assertion` | `test_type=2`(Contains)，值 `application/json`，`test_field=Assertion.response_headers` |

**JSR223 Assertion Groovy 脚本示例**（GET 请求）：

```groovy
import groovy.json.JsonSlurper

def jsonSlurper = new JsonSlurper()
def response = prev.getResponseDataAsString()
def jsonData = jsonSlurper.parseText(response)

// 验证必需字段存在
assert jsonData.message != null : "响应缺少 message 字段"
assert jsonData.timestamp != null : "响应缺少 timestamp 字段"
assert jsonData.service != null : "响应缺少 service 字段"

// 验证字段类型
assert jsonData.message instanceof String : "message 字段类型错误"
assert jsonData.service instanceof String : "service 字段类型错误"
```

**JSR223 Assertion Groovy 脚本示例**（POST 请求，含请求体回显验证）：

```groovy
import groovy.json.JsonSlurper

def jsonSlurper = new JsonSlurper()
def response = prev.getResponseDataAsString()
def jsonData = jsonSlurper.parseText(response)

// 验证必需字段
assert jsonData.message != null : "响应缺少 message 字段"
assert jsonData.receivedData != null : "响应缺少 receivedData 字段"
assert jsonData.timestamp != null : "响应缺少 timestamp 字段"
assert jsonData.service != null : "响应缺少 service 字段"

// 验证字段类型
assert jsonData.message instanceof String : "message 字段类型错误"
assert jsonData.service instanceof String : "service 字段类型错误"
```

---

### 3. 执行接口测试

#### 3.1 启动服务（如需要）

检查目标服务是否已运行：

```bash
# Windows (PowerShell)
netstat -ano | findstr :8080 > $null && Write-Host "qa-service-user 已运行" || Write-Host "qa-service-user 未运行，需要启动"
netstat -ano | findstr :8081 > $null && Write-Host "qa-service-question 已运行" || Write-Host "qa-service-question 未运行，需要启动"

# Linux/macOS
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

#### 3.2 运行 JMeter 测试（CLI 非 GUI 模式）

使用 JMeter CLI 模式运行生成的测试计划：

```powershell
# Windows (PowerShell) - 通过 JMETER_HOME 调用
$jmeterCmd = "$env:JMETER_HOME\bin\jmeter.bat"
& $jmeterCmd -n `
  -t ".asdm/workspace/qahc-harness/testapi/testplans/{service-name}-test-plan.jmx" `
  -l ".asdm/workspace/qahc-harness/testapi/results/{service-name}-results.jtl" `
  -e `
  -o ".asdm/workspace/qahc-harness/testapi/reports/{service-name}" `
  -JresponseTimeThreshold=3000
```

```bash
# Linux/macOS - 通过 JMETER_HOME 调用
$JMETER_HOME/bin/jmeter -n \
  -t .asdm/workspace/qahc-harness/testapi/testplans/{service-name}-test-plan.jmx \
  -l .asdm/workspace/qahc-harness/testapi/results/{service-name}-results.jtl \
  -e \
  -o .asdm/workspace/qahc-harness/testapi/reports/{service-name} \
  -JresponseTimeThreshold=3000
```

**JMeter CLI 参数说明**：

| 参数 | 说明 |
|------|------|
| `-n` | 非 GUI 模式运行 |
| `-t` | 测试计划文件路径（.jmx） |
| `-l` | 测试结果输出文件（.jtl / .csv） |
| `-e` | 测试完成后生成 HTML 报告 |
| `-o` | HTML 报告输出目录 |
| `-Jkey=value` | 覆盖 JMeter 属性（用户自定义变量） |

#### 3.3 解析测试结果

从 JTL 结果文件中解析测试执行情况：

```bash
# 使用 JMeter 插件或自定义脚本解析 JTL 结果
# 统计通过/失败数量
grep -c 'true' .asdm/workspace/qahc-harness/testapi/results/{service-name}-results.jtl  # 通过的请求
grep -c 'false' .asdm/workspace/qahc-harness/testapi/results/{service-name}-results.jtl # 失败的请求
```

展示测试运行结果摘要：

```text
📊 {service-name} 接口测试结果

测试计划: {service-name}-test-plan
总请求数: N
├── ✅ 通过: N
├── ❌ 失败: N
├── ⏱️  平均响应时间: Nms
├── 📈 最小响应时间: Nms
└── 📉 最大响应时间: Nms

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

对每个失败的请求，按以下决策树分析根因：

```
JMeter 请求失败 / 断言失败
  ├── HTTP 状态码 ≠ 预期
  │     ├── 500 → 🔴 API 自身缺陷（服务端异常）
  │     ├── 404 → 🟡 检查路由配置（可能测试计划 URL 错误）
  │     ├── 405 → 🟡 检查 HTTP 方法（可能测试计划方法错误）
  │     ├── 401/403 → 🟡 检查认证配置（可能缺少 token）
  │     └── 4xx 其他 → 结合响应体分析
  ├── 响应体字段缺失/类型错误（JSR223 Assertion 失败）
  │     ├── 响应体为空且有 4xx/5xx → 🔴 API 自身缺陷
  │     ├── 响应体有内容但字段名不匹配 → 🟡 测试断言预期字段需要修正
  │     └── 响应体字段类型与文档不符 → 🔴 API 返回数据格式问题
  ├── Duration Assertion 超时
  │     └── 🟠 性能问题（记录但不阻塞，除非 > 10s）
  └── 连接失败 / Socket 错误
        └── 🟡 环境配置问题（服务未启动 / 端口错误）
```

**标记规则**：

| 标记 | 含义 | 修复方 |
|:----:|------|--------|
| 🔴 | API 自身缺陷 | 修改后端代码 |
| 🟡 | 测试脚本问题 | 修改 JMeter 测试计划中的断言 |
| 🟠 | 性能问题 | 记录在报告中 |

#### 4.2 修复实施

根据分析结果执行修复：

**API 自身缺陷（🔴）**：
1. 定位后端代码中出错的位置
2. 修复代码逻辑
3. 重新编译并重启服务
4. 更新 `develop-log.json` 记录修复动作

**测试脚本问题（🟡）**：
1. 对比实际 API 响应与 JMeter 断言预期
2. 修正 `.jmx` 测试计划中对应的 `Response Assertion` / `JSR223 Assertion` 配置
3. 重新保存测试计划文件

#### 4.3 重新运行

修复完成后重新执行 JMeter 测试，进入下一轮验证。若达到最大重试次数（3次）仍有失败：

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

**JMeter HTML 报告路径**：`.asdm/workspace/qahc-harness/testapi/reports/{service-name}/index.html`

**报告结构**：

```markdown
# API 接口测试报告

## 测试概览

| 项目 | 值 |
|------|-----|
| 测试时间 | {YYYY-MM-DD HH:mm:ss} |
| 测试工具 | Apache JMeter {version} |
| 测试范围 | {全部 / FT-XXX} |
| 服务数量 | N |
| 接口总数 | N |
| 请求总数 | N |
| 通过率 | 100% |
| 修复轮次 | N |

## 接口清单

| # | 服务 | 方法 | 路径 | 断言数 | 状态 |
|---|------|------|------|:-----:|:----:|
| 1 | qa-service-user | GET | /api/xxx | 5 | ✅ |
| ... | ... | ... | ... | ... | ... |

## 各服务测试详情

### qa-service-user (端口 8080)

#### GET /api/test/cors

**请求示例**：
`GET http://localhost:8080/api/test/cors`

**响应示例**：
```json
{
  "message": "CORS test successful",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "service": "qa-service-user"
}
```

**断言结果**：
| 断言 | 类型 | 状态 | 响应时间 |
|------|------|:----:|----------|
| 状态码=200 | Response Assertion | ✅ | - |
| 响应结构验证 | JSR223 Assertion | ✅ | - |
| 字段类型验证 | JSR223 Assertion | ✅ | - |
| Content-Type Header | Response Assertion | ✅ | - |
| 响应时间 < 3000ms | Duration Assertion | ✅ | 15ms |

## 修复记录

（如有修复操作，记录每轮修复的内容）

## 测试资源

| 资源类型 | 路径 |
|----------|------|
| JMeter 测试计划 | .asdm/workspace/qahc-harness/testapi/testplans/ |
| JTL 结果文件 | .asdm/workspace/qahc-harness/testapi/results/ |
| HTML 报告 | .asdm/workspace/qahc-harness/testapi/reports/ |
| 测试数据 | .asdm/workspace/qahc-harness/testapi/data/ |
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
- 测试工具: Apache JMeter
- 测试服务: N 个
- 接口总数: N 个
- 请求总数: N 个
- 通过率: 100%
- 修复轮次: N 次

📄 测试报告: .asdm/workspace/qahc-harness/testapi/results/api-test-report-{date}.md
📊 JMeter HTML 报告: .asdm/workspace/qahc-harness/testapi/reports/
📁 测试计划: .asdm/workspace/qahc-harness/testapi/testplans/
📁 JTL 结果: .asdm/workspace/qahc-harness/testapi/results/

📌 后续可执行 /qahc-harness-verify 进行功能验收审查。
```

---

## Purpose

- 在开发实施完成后，通过自动化方式验证所有 API 接口的正确性
- 利用 Apache JMeter 实现可重复、可版本控制的接口测试
- JMeter 原生支持 CSV/JTL 结果格式，便于 CI/CD 集成和趋势分析
- 通过 JMeter HTML Dashboard 提供丰富的可视化报告
- 智能区分测试断言错误和 API 代码缺陷，精准定位问题
- 通过自动修复→重新测试循环确保接口质量

## Input

- 特性编码 FT-XXX（可选，不指定则测试全部）
- 后端源代码（扫描 Controller 提取 API）
- 过程文档（PRD、CodeResearch、Plan）
- 项目上下文（L1/L2 context）
- Apache JMeter 5.5+

## Output

```json
{
  "phase": "api_testing",
  "status": "success | failed | blocked",
  "test_rounds": 1,
  "tool": "Apache JMeter",
  "summary": {
    "services_tested": 2,
    "total_apis": 5,
    "total_requests": 5,
    "passed": 5,
    "failed": 0,
    "pass_rate": "100%",
    "avg_response_time_ms": 45,
    "min_response_time_ms": 12,
    "max_response_time_ms": 120
  },
  "fixes_applied": {
    "api_fixes": 0,
    "script_fixes": 0
  },
  "report_path": ".asdm/workspace/qahc-harness/testapi/results/api-test-report-{date}.md",
  "jmeter_html_report_paths": [
    ".asdm/workspace/qahc-harness/testapi/reports/qa-service-user/index.html",
    ".asdm/workspace/qahc-harness/testapi/reports/qa-service-question/index.html"
  ],
  "testplan_paths": [
    ".asdm/workspace/qahc-harness/testapi/testplans/qa-service-user-test-plan.jmx",
    ".asdm/workspace/qahc-harness/testapi/testplans/qa-service-question-test-plan.jmx"
  ],
  "jtl_result_paths": [
    ".asdm/workspace/qahc-harness/testapi/results/qa-service-user-results.jtl",
    ".asdm/workspace/qahc-harness/testapi/results/qa-service-question-results.jtl"
  ],
  "timestamp": "ISO 8601 datetime"
}
```

## Downstream Flow

```text
/qahc-harness-develop          ← 上一步：开发实施（全部任务完成）
        │
        ▼
/qahc-harness-testapi           ← 当前步骤：API 接口测试（JMeter）
        │
        ▼
/qahc-harness-verify            ← 下一步：功能验收审查
```

## Spec & Skill Reference

- [Apache JMeter 官方文档](https://jmeter.apache.org/usermanual/) — JMeter 用户手册
- [JMeter CLI 模式指南](https://jmeter.apache.org/usermanual/get-started.html#non_gui) — 非 GUI 模式运行
- [JMeter HTML Dashboard](https://jmeter.apache.org/usermanual/generating-dashboard.html) — 生成 HTML 报告
- [JMeter JSR223 Assertion 指南](https://jmeter.apache.org/usermanual/component_reference.html#JSR223_Assertion) — Groovy 脚本断言
- [markdownlint Skill](../../../.asdm/skills/markdownlint/SKILL.md) — Markdown 格式校验
