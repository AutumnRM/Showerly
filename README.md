# Showerly

极简“澡堂人流”查询应用：无广告、无杂乱功能。

- App 技术栈：Android（Kotlin + Jetpack Compose）/ iOS（Swift 6 + SwiftUI + Liquid Glass）
- 后端再说
- 鸿蒙 NEXT 再再说（
- iOS 26 原生客户端：已加入源码，功能与 Android v0.4 对齐。

## 状态（2026-09-01）
- [x] V1.0（Phase 0）：逆向云达人接口（实测无需鉴权）；安卓 App 已可实时查询。
  - 主屏：底部导航「主页 / 设置」，卡片式浴室页面 + 左右滑动切换 + 下拉刷新；多色有机球按空位/拥挤/爆满占比着色，点球预留浴位图入口。
  - 设置：性别（男/女）、校区（长安/太白，均可用）、深色模式（跟随系统/浅色/深色）。
- [ ] Phase 1：配置后端，实现历史人流曲线。
- [ ] Phase 2-5：人满/损坏判定、趋势预测、人流预警 + Push、鸿蒙适配。

## 目录
- `android/`：安卓客户端（Gradle + Compose）
- `ios/`：iOS 26 客户端（Swift 6 + SwiftUI，包含单元测试与 UI 测试）
- `backend/`：Cloudflare Workers + D1
- `shortcut/`：iOS 快捷指令搭建指南（已废除，iOS 需要 mac 环境开发）
- `harmonyos/`：鸿蒙 NEXT 占位
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

> 版本号：每次 `assembleDebug` 自动把 `versionName` patch +1（versionCode 同步 +1）并写回 `android/app/build.gradle.kts`，与是否 commit 无关（规则 2026-09-02），保证每次测试包版本唯一。

> 提示：校方接口 `cloudman.jinghaojian.net` 的 `campusId=4`（长安）/`campusId=36`（太白）请求**无需鉴权**；App 默认接口地址已写好，无需填 token。请在通用网络（能解析该域名）下测试。

## iOS：从源码构建

环境要求：macOS 26、Xcode 26，以及已安装的 iOS 26 Simulator Runtime。iOS 客户端最低支持 iOS 26，同时适配 iPhone 与 iPad。

```bash
cd ios
xcodebuild -project Showerly.xcodeproj -scheme Showerly \
  -destination 'platform=iOS Simulator,name=iPhone 17' test
```

也可以直接使用 Xcode 打开 `ios/Showerly.xcodeproj`。模拟器构建无需签名；安装到真机前，请在 Signing & Capabilities 中选择自己的 Apple Developer Team。

iOS 客户端不包含第三方依赖，使用 SwiftUI、Observation、Swift Concurrency 与 `URLSession`。为兼容校方接口，请求会保留云达人 Android 客户端已验证的 `os=android`、`versionno=120` 等协议头；这不是设备平台识别逻辑。源码中不保存 token、账号或个人信息。

## 后端快速开始
见 `backend/README.md`。未配置 `SCHOOL_API_URL` 时后端写入演示数据，便于不接校方接口联调。

## 逆向
见 `docs/reverse-engineering.md`：root 安卓 14 手机（一台运行 Pixel OS 的 Mi Mix 2s） + PCAPdroid 抓包，拿到 URL、鉴权与返回结构。

## License
MIT
