# API 接口测试报告

## 测试概览

| 项目 | 值 |
|------|-----|
| 测试时间 | 2026-06-24 14:18:50 UTC |
| 测试工具 | Apache JMeter 5.6.3 |
| 测试范围 | TestController（全部） |
| 服务数量 | 1 |
| 接口总数 | 3（4 个请求） |
| 请求总数 | 4 |
| 通过率 | 100% |
| 修复轮次 | 0 |

## 接口清单

| # | 服务 | 方法 | 路径 | 断言数 | 状态 |
|---|------|------|------|:-----:|:----:|
| 1 | qa-service-user | GET | /api/test/cors | 4 | ✅ |
| 2 | qa-service-user | POST | /api/test/cors | 4 | ✅ |
| 3 | qa-service-user | POST | /api/test/cors (空Body) | 4 | ✅ |
| 4 | qa-service-user | OPTIONS | /api/test/cors | 2 | ✅ |

## 各服务测试详情

### qa-service-user (端口 8080)

#### GET /api/test/cors

**请求示例**：
`GET http://localhost:8080/api/test/cors`

**响应**：200 OK, 360 bytes

**断言结果**：
| 断言 | 类型 | 状态 | 响应时间 |
|------|------|:----:|----------|
| 状态码=200 | Response Assertion | ✅ | - |
| 响应结构验证 (message/timestamp/service) | JSR223 Assertion | ✅ | - |
| Content-Type 包含 application/json | Response Assertion | ✅ | - |
| 响应时间 < 3000ms | Duration Assertion | ✅ | 60ms |

---

#### POST /api/test/cors (带请求体)

**请求示例**：
```json
POST http://localhost:8080/api/test/cors
Content-Type: application/json
{"name":"test","value":123}
```

**响应**：200 OK, 407 bytes

**断言结果**：
| 断言 | 类型 | 状态 | 响应时间 |
|------|------|:----:|----------|
| 状态码=200 | Response Assertion | ✅ | - |
| 响应结构验证 (message/receivedData/timestamp/service) | JSR223 Assertion | ✅ | - |
| Content-Type 包含 application/json | Response Assertion | ✅ | - |
| 响应时间 < 3000ms | Duration Assertion | ✅ | 8ms |

---

#### POST /api/test/cors (空Body)

**请求示例**：
```json
POST http://localhost:8080/api/test/cors
Content-Type: application/json
```

**响应**：200 OK, 384 bytes

**断言结果**：
| 断言 | 类型 | 状态 | 响应时间 |
|------|------|:----:|----------|
| 状态码=200 | Response Assertion | ✅ | - |
| 响应结构验证 (message/timestamp/service) | JSR223 Assertion | ✅ | - |
| Content-Type 包含 application/json | Response Assertion | ✅ | - |
| 响应时间 < 3000ms | Duration Assertion | ✅ | 6ms |

---

#### OPTIONS /api/test/cors

**请求示例**：
`OPTIONS http://localhost:8080/api/test/cors`

**响应**：200 OK, 263 bytes

**断言结果**：
| 断言 | 类型 | 状态 | 响应时间 |
|------|------|:----:|----------|
| 状态码=200 | Response Assertion | ✅ | - |
| 响应时间 < 3000ms | Duration Assertion | ✅ | 4ms |

## 修复记录

无需修复，首次运行全部通过。

## 测试资源

| 资源类型 | 路径 |
|----------|------|
| JMeter 测试计划 | `.asdm/workspace/qahc-harness/testapi/testplans/qa-service-user-test-plan.jmx` |
| JTL 结果文件 | `.asdm/workspace/qahc-harness/testapi/results/qa-service-user-results.jtl` |
| HTML 报告 | `.asdm/workspace/qahc-harness/testapi/reports/qa-service-user/index.html` |
| 测试数据 | `.asdm/workspace/qahc-harness/testapi/data/qa-service-user-test-data.csv` |
