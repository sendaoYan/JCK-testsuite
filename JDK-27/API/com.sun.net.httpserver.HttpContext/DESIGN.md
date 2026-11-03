# com.sun.net.httpserver.HttpContext — 设计案

## 1. API 功能
- 抽象类，表示服务器上一条 URI 路径到 handler 的映射，承载该路径的 filters 和 authenticator。
- 方法：
  - `getHandler()` / `setHandler(HttpHandler)`
  - `getPath()`、`getServer()`
  - `getAttributes()`（可变 `Map<String,Object>`）
  - `getFilters()`（可变 `List<Filter>`）
  - `setAuthenticator(Authenticator)`（返回前一个）、`getAuthenticator()`
- 由 `HttpServer.createContext(...)` 创建。

## 2. Java 用户如何使用
```java
HttpServer s = HttpServer.create(addr, 0);
HttpContext ctx = s.createContext("/app", handler);
ctx.getFilters().add(Filter.beforeHandler("log", e -> ...));
ctx.setAuthenticator(auth);
ctx.getAttributes().put("k", "v");
```
`setHandler` 只能在 handler 尚未设置时调用一次（否则 IAE；null 抛 NPE）；`createContext(path)` 创建空 handler 上下文供后续 `setHandler`。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/sun/net/httpserver/HttpContextImpl.java` 实现 `setHandler` 的「handler already set」校验、`setAuthenticator` 委托给内部 `AuthFilter`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/HttpContextTest.java` 覆盖 handler/attributes/filters/authenticator。
- JDK 测试：`BasicAuthenticatorRealm.java` 中 `context.setAuthenticator(auth)` 与 `server.createContext("/test", handler)`。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、八方法齐全（`HttpContextTest.testWhiteBox`）。
- 黑盒（`HttpContextTest.testBlackBox`）：
  - `createContext(path, handler)`：path/server/handler 断言。
  - attributes put/get/containsKey/remove。
  - filters add/remove（保留服务器初始 authfilter 计数，不做精确 isEmpty）。
  - authenticator 默认 null、set/get 返回前值、清空。
  - `createContext(path)` 空 handler → `setHandler` 后设置；`setHandler(null)` NPE、二次 set IAE。
