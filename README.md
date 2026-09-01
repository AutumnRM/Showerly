# Showerly

极简“澡堂人流”查询应用：无广告、无杂乱功能，全免费技术栈。

- 客户端：Android（Kotlin + Jetpack Compose）+ iOS 快捷指令
- 后端：Cloudflare Workers + D1（全免费）
- 鸿蒙 NEXT：Phase 4 规划（ArkTS / ArkUI）

## 状态
- [x] Phase 0 / V1.0：仓库与安卓实时查询客户端骨架；演示模式可用，待逆向校方接口后接入
- [ ] Phase 1：Workers + D1 轮询；后端骨架已就绪，待配置 D1 / token 后部署
- [ ] Phase 2-5：人满/损坏判定、趋势、军训预警+厂商推送、鸿蒙 NEXT、服务器自托管，详见 `docs/roadmap.md`

## 目录
- `android/`：安卓客户端（Gradle + Compose）
- `backend/`：Cloudflare Workers + D1
- `shortcut/`：iOS 快捷指令搭建指南
- `harmonyos/`：鸿蒙 NEXT 占位与规划
- `docs/`：逆向、架构、路线、安全说明

## 安卓快速开始
用 Android Studio 打开 `android/`，等待 Gradle sync（会自动补齐 wrapper 与 SDK）。首次可开启“演示模式”查看效果；逆向拿到校方接口后，在 App 内“设置”页填入接口地址与认证头即可请求真实数据。

## 后端快速开始
见 `backend/README.md`。未配置 `SCHOOL_API_URL` 时后端写入演示数据，便于不接校方接口联调。

## token 与安全
token 不入库、不编译进 APK：安卓端由用户运行时在“设置”页输入（存 DataStore）；后端用 `wrangler secret` 云端加密存储。永不提交 keystore 与校方凭证，详见 `docs/security.md`。

## 逆向
见 `docs/reverse-engineering.md`：root 安卓 14 手机 + mitmproxy / Frida 抓包校方接口，拿到 URL、鉴权与返回结构。

## License
MIT
