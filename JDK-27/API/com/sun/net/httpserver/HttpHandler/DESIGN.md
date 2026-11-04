# com.sun.net.httpserver.HttpHandler — 设计案

## 1. API 功能
- 函数式接口，处理单个 `HttpExchange`。
- 唯一抽象方法：`void handle(HttpExchange exchange) throws IOException`。
- 由 `HttpServer.createContext(path, handler)` 挂载。

## 2. Java 用户如何使用
```java
server.createContext("/", ex -> {
    byte[] b = "ok".getBytes(UTF_8);
    ex.sendResponseHeaders(200, b.length);
    ex.getResponseBody().write(b);
    ex.close();
});
```
作为函数式接口可用 lambda；`handle` 声明 `IOException`，实现可直接抛出。

## 3. 开源代码实际用法调研
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/Test13.java` 中 `s1.createContext("/", h)`。
- JDK 源码：`HttpHandlers.java` 中 `handleOrElse` 返回 lambda handler，`of` 返回 `exchange -> { ... }`。
- JDK 测试：`BasicAuthenticatorRealm.java` 用 `HttpHandlers.of(200, Headers.of(), "foo")` 作为 handler。

## 4. 测试用例设计 + 自动化实现
- 白盒：接口、`handle` 方法、声明 IOException（`HttpHandlerTest.testWhiteBox`）。
- 黑盒（`HttpHandlerTest.testBlackBox`）：
  - lambda 实例化（函数式接口）。
  - 真实内存 HTTP 回环：handler 读 `getRequestMethod()` 写 body，客户端断言状态 200 + `"handled:GET"`。
