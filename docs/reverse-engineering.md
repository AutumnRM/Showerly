# 校方接口逆向指南

> 目标：从校方 App 中定位“澡堂人数/空位”接口，拿到 URL、鉴权方式和返回粒度。此文档面向 root 安卓 14 手机 + 一台 PC。

## 前置工具
- PC 端：`mitmproxy`（或 HTTP Toolkit / Charles）。本仓库推荐 mitmproxy，命令行可控、免费。
- root 手机：Magisk（或 KernelSU），安装系统级 CA 证书。若校方 App 用证书固定（cert pinning），用 Frida + objection，或 LSPosed + 反证书固定模块。

## 抓包步骤
1. PC 运行 `mitmproxy -p 8888`（或 `mitmweb` 开图形界面）。
2. 手机把 Wi-Fi 代理指向 PC 的 `IP:8888`（本机需与 PC 在同一局域网；用非 root 手机开热点即可）。
3. 打开浏览器访问 `http://mitm.it`，按指示把证书安装为“系统 CA”（root 后可用 Magisk 模块把证书塞进系统目录），否则校方 App 会忽略代理。
4. 校方 App 触发一次“查看澡堂”/“预约”操作，在 mitmproxy 里筛选出对应请求。
5. 记录三项关键信息：
   - 请求 URL（查人数/空位的完整地址，含参数与路径）。
   - 鉴权方式（Header token、Cookie，或带签名的 query 参数）。
   - 响应体结构（是总人数，还是逐浴位状态数组）。

## 证书固定绕过（如需）
- `frida`：`frida -U -f com.example.schoolapp -l bypass.js`，配合 objection 的 `android sslpinning disable`。
- `LSPosed`：安装“JustTrustMe / TrustMeAlready”类模块，重启 App。

## 需要带回的数据
- `SCHOOL_API_URL`：查人数的完整请求 URL。
- `SCHOOL_TOKEN`：鉴权令牌（header 或 cookie 值）。请勿提交到仓库。
- 响应字段映射：把 JSON 里的“人数”“总容量”“时间”等字段名记下来，用于修改 `android/app/src/main/java/com/showerly/app/data/remote/dto/CrowdApiResponse.kt` 与后端 `backend/src/index.js` 的解析逻辑。
- 返回粒度：如果是逐浴位数组，记录每个浴位的状态标识（空闲/占用/故障），用于后续“损坏位判定”。

## 注意事项
- 不要在校方系统上做高频 or 恶意请求；控制轮询频率（每分钟一次足够了）。
- token 有有效期，记录失效时长；若过短，把 login/SSO 流程补进 TODO。
