# 校方接口逆向指南

> 目标：定位校方 App（云达人）中“澡堂人数/空位”接口。本文档基于 root 安卓 14 手机（Mi MIX 2S + Magisk）+ 一台 PC，**已实际跑通**。

## ✅ 已确认（2026-09-01，Phase 0 成果）

- 数据服务器：`cloudman.jinghaojian.net`（阿里云 `8.141.169.103:443`）
- 接口：`GET https://cloudman.jinghaojian.net/bathroom?campusId=4&uid=1250962`
- 鉴权头：`Authorization: Bearer <JWT>`
- 额外必需头：`timestamp`（ms）、`requestid`（`<ts>-<uuid>`）、`os=android`、`versionno=120`、`user-agent=okhttp-okgo/jeasonlzy`
- `sign`、`encrypt` 头（疑似签名/加密）**经 PC 端裸测可不带**：仅带 `authorization` + `timestamp`/`requestid` 等即返回 200，已忽略（2026-09-01 验证）。
- 返回粒度：**一次请求返回该校区全部浴室数组**。示例：
  ```json
  {"code":"200","msg":"成功","data":[{
    "id":31,"name":"博硕2楼男","sex":0,"maxLoad":62,"useCount":3,
    "bookingDeviceCnt":0,"availableBookingDeviceCnt":0
  },{
    "id":46,"name":"东区第一浴室-1层","sex":0,"maxLoad":5,"useCount":8,
    "bookingDeviceCnt":0,"availableBookingDeviceCnt":0
  }]}
  ```
  - `id`：浴室编号；`name`：浴室名；`sex`：0=男/1=女
  - `maxLoad`：容量；`useCount`：当前使用数
  - `bookingDeviceCnt`：预约设备数；`availableBookingDeviceCnt`：可用空位数
  - 空位 = `maxLoad - useCount`（下限 0）。注意个别浴室出现 `useCount>maxLoad`（如 8>5），容量累加时用 `max(maxLoad,useCount)`，避免负空位。

## 待办
- 校区确认：`campusId=3`=太白校区（南校区浴室-男/女）、`campusId=4`=长安校区；其余 campusId 多属其它学校。太白接口存在问题（2026-09-02），App 已置灰，待更新后启用。
- 确认 JWT `exp` 判断 token 有效期；若过短，把 login/SSO 列入 TODO。
- ~~确认 `sign`/`encrypt` 用途~~ 已解决：PC 端裸测可不带，Workers 可直接代理。

## 实操路径（PCAPdroid，已跑通）

### 工具
- 手机：PCAPdroid v2.0.0（GitHub 官方 APK）+ PCAPdroid-mitm addon v2.4（arm64）。
- root 授权给 PCAPdroid，把它的 CA 装入系统信任区（本机直接把 `81c450f1.0` 拷进 `/system/etc/security/cacerts/` 和 `/apex/com.android.conscrypt/cacerts/`）。

### 步骤
1. PCAPdroid 设置：开启 **TLS decryption**，目标应用选**云达人**，dump 模式选 **无转储**（导出用「连接列表 → ⋮ → 另存为 CSV」，或逐条看 HTTP 标签）。
2. 点开始，允许 VPN；进云达人打开 **澡堂/浴室** 页面，下拉刷新。
3. 停止后，连接列表里找 `cloudman.jinghaojian.net:443` 且**解密成功**的那条，点进「HTTP」标签看请求 URL 与响应 JSON。
4. 广告/统计域名直接无视：`ulogs.umeng.com`、`apmplus.volces.com`、`fancyapi.com`、`beizi.biz`、`lrtb.net`、`zhangyuyidong.cn`。

### 证书固定绕过（如需）
- on-device 无法解密时改用 `frida -U -f com.jhj.cloudman -l bypass.js`，或 LSPosed + JustTrustMe/TrustMeAlready，重启 App。
- 本案例未到证书固定那一步：把 CA 装成**系统 CA** 后即成功解密。

## 需要带回的数据
- `SCHOOL_API_URL`：完整请求 URL（含 `campusId` 与 `uid` 参数）。
- `SCHOOL_TOKEN`：`Bearer <JWT>`。请勿提交到仓库。
- 响应字段映射已确认，对应 `android/.../dto/CrowdApiResponse.kt` 与 `backend/src/index.js`。

## 注意事项
- 控制轮询频率：每分钟一次足够，避免给校方系统造成压力。
- token 有有效期；记录失效时长，过短则把 login/SSO 补入 TODO。
- 涉及校方数据的接口，仅供学习/校内小范围使用，勿公开真实 token 与 PII。


