# API 接口测试报告

## 测试概览

| 项目 | 值 |
|------|-----|
| 测试时间 | 2026-06-16 12:44 |
| 测试范围 | TestController（server/qa-service-user） |
| 服务 | qa-service-user (端口 8080) |
| 接口总数 | 3（GET + POST + OPTIONS） |
| 测试用例总数 | 4 |
| 断言总数 | 19 |
| 通过率 | **100%** ✅ |
| 修复轮次 | 0（首次即全部通过） |

## 接口清单

| # | 方法 | 路径 | 测试用例数 | 断言数 | 状态 |
|---|:----:|------|:----------:|:------:|:----:|
| 1 | GET | `/api/test/cors` | 1 | 6 | ✅ |
| 2 | POST | `/api/test/cors`（带请求体） | 1 | 7 | ✅ |
| 3 | POST | `/api/test/cors`（空请求体） | 1 | 3 | ✅ |
| 4 | OPTIONS | `/api/test/cors` | 1 | 3 | ✅ |

## 各接口测试详情

### GET /api/test/cors — CORS 测试

**请求**：`GET http://localhost:8080/api/test/cors`

| 测试用例 | 状态 | 响应时间 |
|----------|:----:|----------|
| [状态码] 返回 200 | ✅ | - |
| [响应结构] 包含必需字段 | ✅ | - |
| [字段值] service 为 qa-service-user | ✅ | - |
| [字段类型] 字段类型正确 | ✅ | - |
| [Header] Content-Type 包含 application/json | ✅ | - |
| [性能] 响应时间 < 3000ms | ✅ | 87ms |

### POST /api/test/cors — CORS POST 测试（带请求体）

**请求**：`POST http://localhost:8080/api/test/cors`
**请求体**：`{"testKey": "testValue", "name": "API Test"}`

| 测试用例 | 状态 | 响应时间 |
|----------|:----:|----------|
| [状态码] 返回 200 | ✅ | - |
| [响应结构] 包含必需字段 | ✅ | - |
| [字段值] message 正确 | ✅ | - |
| [消息回显] receivedData 与请求体一致 | ✅ | - |
| [字段类型] 字段类型正确 | ✅ | - |
| [Header] Content-Type 包含 application/json | ✅ | - |
| [性能] 响应时间 < 3000ms | ✅ | 17ms |

### POST /api/test/cors — 空请求体测试

**请求**：`POST http://localhost:8080/api/test/cors`
**请求体**：`""`（空）

| 测试用例 | 状态 | 响应时间 |
|----------|:----:|----------|
| [状态码] 返回 200（空请求体可接受） | ✅ | - |
| [响应结构] 包含必需字段 | ✅ | - |
| [空请求体] receivedData 为 null | ✅ | 2ms |

### OPTIONS /api/test/cors — CORS 预检

**请求**：`OPTIONS http://localhost:8080/api/test/cors`
**Headers**：`Origin: http://localhost:5173`, `Access-Control-Request-Method: GET`

| 测试用例 | 状态 | 响应时间 |
|----------|:----:|----------|
| [状态码] 返回 200（CORS预检通过） | ✅ | - |
| [Header] CORS 头存在 | ✅ | - |
| [性能] 响应时间 < 3000ms | ✅ | 5ms |

## 修复记录

无修复操作（首次运行即全部通过）。

## 测试资源

| 资源类型 | 路径 |
|----------|------|
| Collection | `.asdm/workspace/qahc-harness/testapi/collections/qa-service-user.postman_collection.json` |
| Environment | `.asdm/workspace/qahc-harness/testapi/environments/local.postman_environment.json` |
| Results JSON | `.asdm/workspace/qahc-harness/testapi/results/qa-service-user-results.json` |
