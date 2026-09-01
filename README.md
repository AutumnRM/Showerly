# Showerly

极简“澡堂人流”查询应用：无广告、无杂乱功能，全免费技术栈。

- 客户端：Android（Kotlin + Jetpack Compose）+ iOS 快捷指令
- 后端：Cloudflare Workers + D1（全免费）
- 鸿蒙 NEXT：Phase 4 规划（ArkTS / ArkUI）

## 状态（2026-09-01）
- [x] V1.0（Phase 0）：逆向校方「澡堂洗浴」接口（`campusId=4`，实测无需鉴权）；安卓 App 已可实时查询。
  - 主屏：底部导航「主屏 / 设置」，卡片式浴室（每浴室：呼吸球、人数、空位、容量、状态色）+ 左右滑动切换 + 下拉刷新。
  - 设置：性别（男/女）、校区（长安可用，太白待逆向）、主题色调色盘、深色模式（跟随系统/浅色/深色）；改动即时生效、无需保存。
- [ ] Phase 1：Workers + D1 轮询；后端骨架已就绪，待配置 D1 / 部署。
- [ ] Phase 2-5：人满/损坏判定、趋势、军训预警+厂商推送、鸿蒙 NEXT、服务器自托管，详见 `docs/todo.md`。

## 目录
- `android/`：安卓客户端（Gradle + Compose）
- `backend/`：Cloudflare Workers + D1
- `shortcut/`：iOS 快捷指令搭建指南
- `harmonyos/`：鸿蒙 NEXT 占位与规划
- `docs/`：逆向、架构、导航、浴室目录、路线、TODO、安全说明

## 安卓：从源码构建
环境要求：
- JDK 17（Gradle 8.10.2 与 AGP 8.7.3 不支持更高主版本；本项目用 Microsoft OpenJDK 17）。
- Gradle 8.10.2（或用 `gradlew`，需先能自动下载 wrapper）。
- Android SDK：`platforms;android-35`、`build-tools;34.0.0`、`platform-tools`（AGP 会在 licenses 已接受时自动补装缺失组件）。
- `android/local.properties` 写入：
  ```properties
  sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk
  ```

构建调试 APK：
```powershell
$env:JAVA_HOME = "C:\path\to\jdk-17"
cd android
gradle :app:assembleDebug
# 输出：android/app/build/outputs/apk/debug/app-debug.apk
```

安装到手机：
```powershell
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.showerly.app/.MainActivity
```

> 提示：校方接口 `cloudman.jinghaojian.net` 当前的 `campusId=4` 请求**无需鉴权**；App 默认接口地址已写好，无需填 token。请在通用网络（能解析该域名）下测试；若手机处于热点/无网环境会提示“无法解析主机”。

## 后端快速开始
见 `backend/README.md`。未配置 `SCHOOL_API_URL` 时后端写入演示数据，便于不接校方接口联调。

## token 与安全
token 不入库、不编译进 APK：安卓端由用户运行时在“设置”页输入（存 DataStore）；后端用 `wrangler secret` 云端加密存储。永不提交 keystore 与校方凭证，详见 `docs/security.md`。

## 逆向
见 `docs/reverse-engineering.md`：root 安卓 14 手机 + PCAPdroid 抓包校方接口，拿到 URL、鉴权与返回结构。

## License
MIT