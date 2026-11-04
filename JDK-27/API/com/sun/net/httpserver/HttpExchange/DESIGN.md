# com.sun.net.httpserver.HttpExchange — 设计案

## 1. API 功能
- 抽象类，表示一个已接收的 HTTP 交换（请求+响应），实现 `AutoCloseable` 与 `Request`。
- 请求侧：`getRequestURI()`、`getRequestMethod()`、`getRequestHeaders()`、`getRequestBody()`、`getRemoteAddress()`、`getProtocol()`、`getPrincipal()`。
- 响应侧：`getResponseHeaders()`、`getResponseBody()`、`sendResponseHeaders(int, long)`、`getResponseCode()`。
- 状态：`getAttribute/setAttribute`、`setStreams`、`getLocalAddress()`、`getHttpContext()`、`close()`。
- JDK-27 新增常量：`RSPBODY_EMPTY = -1L`、`RSPBODY_CHUNKED = 0L`。

## 2. Java 用户如何使用
典型 handler 回环：
```java
void handle(HttpExchange ex) throws IOException {
    byte[] body = ex.getRequestBody().readAllBytes();
    ex.getResponseHeaders().set("Content-Type", "text/plain");
    ex.sendResponseHeaders(200, resp.length);
    ex.getResponseBody().write(resp);
    ex.close();
}
```
`sendResponseHeaders(rCode, -1)` 表示无响应体；`0` 表示 chunked；`>0` 表示定长。

## 3. 开源代码实际用法调研
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/ExchangeAttributeTest.java` 中 `exchange.sendResponseHeaders(200, -1)`、`exchange.setAttribute(...)`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/EchoHandler.java` 中 `t.sendResponseHeaders(200, in.length == 0 ? -1 : in.length)`。
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/sun/net/httpserver/ExchangeImpl.java` 实现 `getResponseCode`（未发送前 `-1`，发送后为 rCode）。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、实现 Request/AutoCloseable、16 个方法、`RSPBODY_EMPTY==-1`、`RSPBODY_CHUNKED==0`（`HttpExchangeTest.testWhiteBox`）。
- 黑盒（`HttpExchangeTest.testBlackBox`）：真实 handler 内捕获并事后断言：
  - method/URI path/protocol/local/remote/context 非空与值。
  - responseHeaders 可写、requestHeaders/requestBody 非空。
  - principal 无认证时 null；attribute set/get 回环。
  - `getResponseCode()` 发送前 -1、发送后 200。
  - 客户端侧断言 200 + body。
