# java.net.http.WebSocket.Builder 设计文档

## 1. API 功能

WebSocket 构造器：`header(name, value)`、`connectTimeout(Duration)`、`subprotocols(String, String...)`、
`buildAsync(URI, Listener)`。

## 2. 使用方式

`client.newWebSocketBuilder().subprotocols("chat").connectTimeout(Duration.ofSeconds(5))
.buildAsync(URI.create("ws://host/path"), listener)`。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/websocket/OpeningHandshake.java`（校验先于 IO）。

## 4. 测试用例设计 + 易错规范细节

文件：`BuilderTest.java`（只测参数校验，全部在 IO 之前失败）。

- 形状：4 个抽象方法、subprotocols varargs、返回类型。
- 同步 NPE（probed）：`buildAsync(null, l)` → NPE "uri"、`buildAsync(uri, null)` → NPE "listener"；
  setter null NPE。
- 参数校验失败以 failed future 呈现（probed 精确消息）：
  非法头名 "X Y"/含 LF 头值 → IAE `invalid header name/value`；
  5 个受限握手头（Sec-WebSocket-Version/Protocol/Key/Accept/Extensions）→ IAE `Illegal header: <name>`；
  connectTimeout ZERO/-1ms → IAE `Invalid duration: PT0S/PT-0.001S`；
  子协议空格/空串/重复/含 TAB → IAE `Bad subprotocol syntax`/`Duplicating subprotocol: x`；
  URI scheme http/https/无 scheme/含 fragment/无 host → 对应 IAE。
- builder 状态按实例隔离（probed）；链式 setter 返回 this。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。
