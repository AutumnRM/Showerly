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
- `normalize()` 里的字段按校方实际响应调整；逐浴位状态后续在 Phase 2 增加 `bay_status`。
- Cron 每分钟一次，位于 Cloudflare 免费额度内。若大陆网络无法访问 `workers.dev`，见 docs/roadmap.md 的服务器自托管 TODO。
