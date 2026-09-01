# 鸿蒙 NEXT 版（Phase 4 占位）

> 目标：面向纯血鸿蒙（HarmonyOS NEXT，只支持 ArkTS/ArkUI）的独立客户端，复用同一套 Cloudflare Workers API。

## 规划
- 语言/UI：ArkTS + ArkUI（声明式）。
- 网络：`@ohos.net.http`，请求 `https://<your-worker>/api/current` 等。
- 数据模型：对应 `backend` 的 `/api/*` 返回结构，token 与安卓端一致地由用户在运行时输入，不入库、不编进 HAP。
- 产物：`.hap`，用于 HarmonyOS NEXT 设备调试与侧载。
- 与安卓端无代码复用，仅共享接口契约与 docs/architecture.md 的数据流说明。

## 后续
Phase 4 时再补齐 `entry/src/main/ets` 工程结构、`module.json5` 与权限声明；当前先记录接口契约，避免重复设计。
