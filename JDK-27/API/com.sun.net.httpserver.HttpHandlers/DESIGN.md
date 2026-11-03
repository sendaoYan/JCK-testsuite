# com.sun.net.httpserver.HttpHandlers — 设计案

## 1. API 功能
- final 工具类（私有构造器），提供现成的 `HttpHandler` 工厂。
- `of(int statusCode, Headers headers, String body)`：固定状态+头+UTF-8 体的静态响应 handler；先读尽请求体，再发送响应；HEAD 请求只写 Content-Length 不写体；空体发送 `-1`；状态码须为 100..999。
- `handleOrElse(Predicate<Request>, HttpHandler handler, HttpHandler fallbackHandler)`：按谓词路由到 handler 或 fallback。

## 2. Java 用户如何使用
```java
var json = HttpHandlers.of(200, Headers.of("Content-Type","application/json"), "{...}");
var handler = HttpHandlers.handleOrElse(r -> r.getRequestMethod().equals("GET"), json, notAllowed);
server.createContext("/", handler);
```

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/HttpHandlers.java` 实现 `of` 与 `handleOrElse`。
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/sun/net/httpserver/simpleserver/FileServerHandler.java` 用 `HttpHandlers.of(501, Headers.of(), "")` 与 `HttpHandlers.of(405, Headers.of("Allow","HEAD, GET"), "")` 作 fallback。
- JDK 测试：`BasicAuthenticatorRealm.java` 用 `HttpHandlers.of(200, Headers.of(), "foo")`。

## 4. 测试用例设计 + 自动化实现
- 白盒：final、私有构造器、`of/handleOrElse`（`HttpHandlersTest.testWhiteBox`）。
- 黑盒（`HttpHandlersTest.testBlackBox`）：
  - `of(200, Headers.of("Content-Type","text/plain"), "body")` 真实回环：状态 200、body、Content-Type 头复制。
  - 空体 `of(204, ..., "")`：无 body。
  - HEAD：Content-Length 设置但无 body。
  - `handleOrElse` 按 GET/POST 路由到不同 handler（200/405）。
  - 参数校验：状态码 99/1000 抛 IAE；null headers/body/predicate/handler/fallback 抛 NPE。
