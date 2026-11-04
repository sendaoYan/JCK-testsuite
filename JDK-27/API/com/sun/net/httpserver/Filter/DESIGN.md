# com.sun.net.httpserver.Filter — 设计案

## 1. API 功能
- 抽象类，用于对请求做前处理/后处理，按链组织并挂在 `HttpContext` 上。
- 核心方法：`abstract void doFilter(HttpExchange, Filter.Chain)`、`abstract String description()`。
- 静态工厂（JDK 17/18）：
  - `beforeHandler(String, Consumer<HttpExchange>)`：handler 之前执行。
  - `afterHandler(String, Consumer<HttpExchange>)`：handler 之后执行。
  - `adaptRequest(String, UnaryOperator<Request>)`：用 `Request.with` 改造请求状态。
- 嵌套 `Filter.Chain`：`doFilter(HttpExchange)` 调用下一个 filter 或最终 handler。

## 2. Java 用户如何使用
```java
var f = Filter.beforeHandler("set Foo", e -> e.getResponseHeaders().set("Foo", "Bar"));
context.getFilters().add(f);
```
`beforeHandler` 操作先于 handler；`afterHandler` 操作后于 handler（可读 `e.getResponseCode()`）；`adaptRequest` 把 `Request.with("X", List.of("v"))` 的结果作为 handler 看到的请求头。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/Filter.java` 定义三个静态工厂，`adaptRequest` 内部用 `new DelegatingHttpExchange(exchange)` 覆盖 `getRequestURI/getRequestMethod/getRequestHeaders`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/FilterTest.java` 大量 `server.createContext("/", handler).getFilters().add(filter)`，覆盖 before/after/adapt 三类 filter 的真实回环。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/LogFilter.java` 自定义 filter 做日志。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、`doFilter/description`、三个静态工厂、嵌套 Chain 存在（`FilterTest.testWhiteBox`）。
- 黑盒（`FilterTest.testBlackBox`）：
  - 工厂 description 直查 + null 参数 NPE。
  - `beforeHandler` 在 handler 前写 exchange 属性，handler 读回 → body 断言 "BEFORE"。
  - `afterHandler` 在 handler 后读 `getResponseCode()`，用 AtomicInteger 捕获 202。
  - `adaptRequest` 用 `Request.with` 注入 `X-Foo`，handler 读 `getRequestHeaders().getFirst("X-Foo")=="bar"`。
  - 每个场景都用 ephemeral 端口内存服务器 + finally stop(0)。
