# java.net.http.WebSocketHandshakeException 设计文档

## 1. API 功能

WebSocket 握手失败时抛出的异常，final 类，继承 `IOException`，并携带一个 `HttpResponse<?>`。

- 构造器：`WebSocketHandshakeException(HttpResponse<?> response)`（无 message）。
- 查询：`getResponse()` 返回触发失败的 HTTP 响应（可用于读取状态码、headers、body）。
- 链方法：协变覆盖的 `initCause(Throwable)` 返回 `WebSocketHandshakeException`（而非常规 `Throwable`）。

## 2. Java 用户如何使用

- 捕获型用法：`WebSocket.Builder.buildAsync(...)` 握手失败时 `catch (WebSocketHandshakeException e)`，
  用 `e.getResponse().statusCode()` 读取 HTTP 状态、`e.getResponse().headers()` 读响应头。
- 链式诊断：`e.initCause(root)` 后 `getCause()` 取根因。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/jdk/internal/net/http/websocket/`）：握手阶段返回非 101 状态或异常时抛出该异常并携带 `HttpResponse`。
- 社区常见形态：WebSocket 客户端按 `getResponse().statusCode()` 区分 4xx/5xx 失败原因。

## 4. 测试用例设计 + 自动化实现

文件：`WebSocketHandshakeExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `IOException` / `HttpResponse` 构造器 / `getResponse()`。
- 黑盒：用 `Proxy` 生成一个 `HttpResponse`（`statusCode()`→403）→构造→`getResponse()` 返回同一实例→
  `IOException` 层级→`getMessage()==null`→无 cause→堆栈非空→`initCause` 返回 this 且 cause 可读→
  二次 `initCause` 抛 `IllegalStateException`。
