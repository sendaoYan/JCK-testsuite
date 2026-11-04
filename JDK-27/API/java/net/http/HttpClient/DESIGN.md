# java.net.http.HttpClient 设计文档

## 1. API 功能

发送 HTTP 请求、接收响应的抽象客户端，实现 `AutoCloseable`。

- 工厂：`newHttpClient()`（默认配置）、`newBuilder()`（自定义配置）。
- 查询：`version()`、`followRedirects()`、`connectTimeout()`、`proxy()`、`executor()`、`authenticator()`、
  `cookieHandler()`、`sslContext()`、`sslParameters()`。
- 请求：`send(HttpRequest, BodyHandler)`（同步）、`sendAsync(...)`（异步，两个重载）。
- WebSocket：`newWebSocketBuilder()`。
- 生命周期：`shutdown()`、`shutdownNow()`、`awaitTermination(Duration)`、`isTerminated()`、`close()`。

## 2. Java 用户如何使用

- 组合链路：`HttpClient.newBuilder().followRedirects(ALWAYS).connectTimeout(d).build()`
  → `client.send(HttpRequest.newBuilder(uri).build(), BodyHandlers.ofString())` → 读 `HttpResponse`。
- 生命周期：`shutdown()` → `awaitTermination(d)` → `isTerminated()`；或 try-with-resources `close()`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/java/net/http/HttpClient.java` 与
  `jdk/internal/net/http/HttpClientImpl.java`）：`newHttpClient()` 使用 daemon 线程池与 `SelectorManager`；
  `shutdown/awaitTermination/isTerminated` 由 `HttpClientImpl` 实现，基于 `shutdownRequested`/`isAlive` 状态。
- 社区常见形态：Spring/Netty 之外的 Java 原生 HTTP 客户端、JDK 内置工具与测试大量使用 `newBuilder().build()` + `send`。

## 4. 测试用例设计 + 自动化实现

文件：`HttpClientTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / `AutoCloseable` / protected 无参构造器 / 关键方法面（send/sendAsync/shutdown/…）。
- 黑盒（不真发请求）：`newHttpClient()` 默认属性 → `newBuilder().build()` 默认属性 →
  显式 builder 配置读回（followRedirects/version/connectTimeout/proxy/executor）→
  `shutdown→awaitTermination→isTerminated` 与 `shutdownNow` 生命周期。

## 6. 缺口分析（本批深化）

原 52 条断言只覆盖默认值与基本生命周期。本批追加至 121 条：

- 访问器 round-trip：sslContext/cookieHandler/authenticator/executor/proxy identity；
  sslParameters 防御性拷贝（内容相等非同实例）；NO_PROXY 常量；Version.HTTP_3 与三种 Redirect 可配置读回。
- 生命周期深挖：awaitTermination(ZERO/负值) 不抛 IAE 返回 false（probed）、null → NPE；
  shutdown/close 幂等；终止后 send() → IOException "closed"；完全终止后 awaitTermination(ZERO) 返回 true（probed）。
- 签名核对：send 声明 IOException+InterruptedException、恰两个 sendAsync 重载、
  JDK-27 无 localAddress() 读访问器（仅 Builder 侧，probed）。
- 边界：connectTimeout(1ns)；全部 Optional 访问器默认空。
