# Showerly for iOS

原生 iOS 26 客户端，使用 Swift 6、SwiftUI、Observation、Swift Concurrency 和 Liquid Glass，无第三方依赖。

## Targets

- `Showerly`：iPhone/iPad App
- `ShowerlyTests`：模型、网络、缓存、设置和状态管理单元测试
- `ShowerlyUITests`：主页、设置、分页和错误态 UI 测试

## Build

安装 Xcode 26 和 iOS 26 Simulator 后，在 Xcode 中打开 `Showerly.xcodeproj`，或执行：

```bash
xcodebuild -project Showerly.xcodeproj -scheme Showerly \
  -destination 'platform=iOS Simulator,name=iPhone 17' test
```

模拟器不需要签名。真机运行时选择自己的 Development Team；默认 Bundle ID 为 `com.showerly.app`。
