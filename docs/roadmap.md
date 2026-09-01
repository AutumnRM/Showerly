# 路线图

## Phase 0 / V1.0 — 逆向 + 实时查询客户端（当前）
- 用 root 手机抓包逆向校方接口。
- 安卓端：单页“人流” + 设置页（输入 token/校方接口）+ 下拉刷新 + 错误空态 + Demo 模式。
- iOS 快捷指令：同一套请求，读人数/提醒。

## Phase 1 — Cloudflare Workers + D1
- Cron 每分钟拉校方接口写 D1；暴露 `/api/current`、`/api/history`、`/api/status`。
- 客户端及快捷指令切换到 Workers base URL。

## Phase 2 — 人满/损坏判定 + 趋势
- 按“连续空置样本占比”区分损坏浴位与翻转空位，剔除损坏位后按占用率阈值判定爆满。
- 移动平均 + 线性回归 + 历史同日对比，输出预测。

## Phase 3 — 军训预警 + 跨用户推送
- Workers 定时推送“军训是否结束？”并收集应答；命中高人流后经厂商通道推送。
- 依赖后期志愿者的厂商通道设备；自有设备回退 Web Push/本地通知。

## Phase 4 — 鸿蒙 NEXT
- ArkTS/ArkUI 独立实现，复用同一套 Workers API，产出 HAP。

## Phase 5 — 服务器 TODO（最后）
- 申请到计协服务器/树莓派后，把轮询迁到校园网自托管。
