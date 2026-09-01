# 安全与机密

## 绝不提交
- 校方 token（`SCHOOL_TOKEN`、设置页输入的 token）。
- Android 签名 keystore：`*.jks`、`*.keystore`、`keys.properties`。
- IDE 私有配置：`local.properties`、`secrets.local.properties`、`.idea/`。
- 后端本地机密：`backend/.dev.vars`、`.env`、`.wrangler/`。

## 存放位置
- 安卓：用户运行时在“设置”页输入 token，存 DataStore；不要写死进代码或 BuildConfig。
- 后端：用 `wrangler secret put SCHOOL_TOKEN` 存入 Cloudflare Secret；本地开发用 `.dev.vars`（已忽略）。
- 示例模板：`backend/.dev.vars.example` 只含占位符，不含真实值。

## 开源风险
- 公开仓库意味着任何人可读代码。校方接口属于逆向产物，请勿在 README 或 issues 中贴出校方真实凭证、其他同学 PII，或鼓励滥用的内容。
- 若校方接口访问策略敏感，可考虑仍在公开代码的同时，仅由维护者持有 token，客户端通过后端获取数据。
