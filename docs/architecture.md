# 架构说明

## 组件
- 安卓客户端：Kotlin + Jetpack Compose，V1.0 直连校方接口；后期切换到 Cloudflare Workers。
- iOS 快捷指令：与客户端共用同一套请求/解析，仅作展示。
- Cloudflare Workers + D1：后期每分钟轮询校方接口，写入历史，对外提供查询 API。

## 数据流（后期）
```
校方接口 <- Worker (Cron /1min) -> D1 -> /api/current /api/history /api/status
安卓 / 快捷指令 ---- HTTP GET ----> Workers 公开接口
```

## 接口
- `GET /api/current`：最新一条快照 `{ ts, total, capacity, status }`
- `GET /api/history?limit=60`：最近 N 条快照
- `GET /api/status`：基本状态与最近更新时间
- 预留：`GET /api/predict`（趋势）

## D1 表
- `samples(id INTEGER PK AUTOINCREMENT, ts INTEGER, total INTEGER, capacity INTEGER, status TEXT, raw TEXT)`

## 机密处理
- 安卓：token 由用户运行时在“设置”页输入，存 DataStore；不入库、不编进 APK。
- 后端：token 存 Cloudflare Worker Secret（`wrangler secret put SCHOOL_TOKEN`）；本地开发用 `backend/.dev.vars`（已在 `.gitignore`）。
