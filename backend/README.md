# Showerly 后端（Cloudflare Workers + D1）

每分钟轮询校方接口写入 D1，并向内提供查询 API。

## 本地

```bash
npm install
cp .dev.vars.example .dev.vars   # 填 SCHOOL_API_URL / SCHOOL_TOKEN
npm run dev                       # 本地启动，Cron 需 wrangler dev 触发
```

`.dev.vars` 已加入 `.gitignore`，不要提交真实 token。

## 部署

```bash
wrangler login
npm run d1:create                 # 创建 D1，把输出到 database_id 填入 wrangler.toml
npm run d1:apply                  # 建表
npx wrangler secret put SCHOOL_TOKEN   # 上传校方令牌（云端加密存储）
npx wrangler secret put SCHOOL_API_URL # 上传接口地址（如需）
npm run deploy
```

## 说明
- 未设置 `SCHOOL_API_URL` 时后端写入演示数据，方便不接校方接口联调。
- `SCHOOL_API_URL` 需为完整地址，如 `https://cloudman.jinghaojian.net/bathroom?campusId=4&uid=1250962`。
- `SCHOOL_TOKEN` 填 JWT，可带或不带 `Bearer ` 前缀（后端自动补）。
- 校方另有 `sign`/`encrypt` 请求头，但经 PC 端裸测可不带：仅 `authorization` + `timestamp`/`requestid` 等即返回 200，已忽略（2026-09-01 验证）。
- `normalize()` 已适配校方数组响应：`total = ΣuseCount`，`capacity = Σmax(maxLoad,useCount)`，并输出每个浴室的 `vacant`。逐浴位状态与“损坏位/爆满”判定在 Phase 2 增加。
- 一次请求只返回**某个 `campusId`** 的浴室。要覆盖全校需枚举 campusId（见 docs/reverse-engineering.md 待办）。
- Cron 每分钟一次，位于 Cloudflare 免费额度内。若大陆网络无法访问 `workers.dev`，见 docs/roadmap.md 的服务器自托管 TODO。

