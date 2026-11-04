# com.sun.net.httpserver.HttpServer — 设计案

## 1. API 功能
- 抽象类，内存 HTTP 服务器（由 `HttpServerProvider` 创建）。
- 工厂：`create()`（未绑定，需 `bind`）、`create(InetSocketAddress, int backlog)`（立即绑定）、`create(InetSocketAddress, int, String path, HttpHandler, Filter...)`（绑定并建初始上下文）。
- 生命周期：`bind(InetSocketAddress, int)`、`start()`、`stop(int delay)`。
- 上下文：`createContext(String, HttpHandler)`、`createContext(String)`、`removeContext(String)`（未知路径抛 IAE）、`removeContext(HttpContext)`。
- 线程池：`setExecutor(Executor)` / `getExecutor()`。
- 查询：`getAddress()`。

## 2. Java 用户如何使用
```java
HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
s.createContext("/app", handler);
s.start();
// ... request ...
s.stop(0);
```
`create()`（无参）返回未绑定服务器，需 `bind` 后 `start`。

## 3. 开源代码实际用法调研
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/CreateHttpServerTest.java` 覆盖 `HttpServer.create()` 与 `HttpServer.create(addr, 123)`。
- JDK 测试：`BasicAuthenticatorRealm.java`、`FilterTest.java`、`Test8.java` 等大量 `HttpServer.create(new InetSocketAddress(LOOPBACK_ADDR, 0), 0)` + `createContext` + `start`。
- JDK 源码：`HttpServer.java` 的工厂委托给 `HttpServerProvider.provider().createHttpServer(...)`。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、`create/bind/start/stop/createContext/removeContext/getAddress/setExecutor/getExecutor`（`HttpServerTest.testWhiteBox`）。
- 黑盒（`HttpServerTest.testBlackBox`）：
  - `create(ephemeral)` 绑定 ephemeral 端口；`create()` 未绑定 → `bind` 后 ephemeral。
  - 真实回环：`createContext` 路径/handler、无 handler 上下文、`start` 后请求、`removeContext(String/HttpContext)` 后 404、未知路径 IAE。
  - executor 默认 null、set/get、清空，finally shutdown executor。
