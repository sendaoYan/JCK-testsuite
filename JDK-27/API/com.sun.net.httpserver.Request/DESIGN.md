# com.sun.net.httpserver.Request — 设计案

## 1. API 功能
- 接口，表示 HTTP 交换的不可变请求状态视图（JDK 18）。
- 抽象方法：`getRequestURI()`、`getRequestMethod()`、`getRequestHeaders()`（只读 Headers）。
- 默认方法：`Request with(String headerName, List<String> headerValues)`：返回一个相同请求外加一个头的新 Request；若同名头已存在则不替换；headerName/headerValues 及其元素为 null 抛 NPE。
- `HttpExchange` 实现该接口。

## 2. Java 用户如何使用
```java
Request adapted = exchange.with("X-Foo", List.of("bar"));
String v = adapted.getRequestHeaders().getFirst("X-Foo");
```
常用于 `Filter.adaptRequest` 中注入/改写 handler 看到的请求头。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/Request.java` 的默认 `with` 用 `Headers.of(h)` 构造不可变视图。
- JDK 源码：`Filter.adaptRequest` 内 `requestOperator.apply(exchange)` 后读取 `request.getRequestURI/getRequestMethod/getRequestHeaders`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/FilterTest.java` 的 adaptRequest 用 `r.with(...)` 注入头。

## 4. 测试用例设计 + 自动化实现
- 白盒：接口、四方法、`with` 为 default（`RequestTest.testWhiteBox`）。
- 黑盒（`RequestTest.testBlackBox`）：
  - 匿名实现 + `with`：新增头、保留原头、多值、保留 URI/method、同名头不替换、null 参数 NPE。
  - 真实 `HttpExchange` 上调用 `with`，断言 `HttpExchange implements Request.with` 生效。
