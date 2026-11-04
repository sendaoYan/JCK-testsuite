# com.sun.net.httpserver.Headers — 设计案

## 1. API 功能
- HTTP 请求/响应头的表示，实现 `Map<String,List<String>>`，键大小写不敏感（normalize：首字母大写、其余小写）。
- 便捷方法：`getFirst(String)`、`add(String,String)`、`set(String,String)`。
- 构造：无参（空，mutable）；`Headers(Map)` 拷贝构造（mutable，JDK 18）。
- 静态工厂：`of(String...)`、`of(Map)` 返回不可变 Headers（`UnmodifiableHeaders`，任何 mutator 抛 `UnsupportedOperationException`）。
- 完整 Map 方法：`put/putAll/remove/clear/keySet/values/entrySet/replaceAll`，以及 `equals/hashCode/toString`。
- 空值拒绝：null key/value 抛 NPE；key/value 含 CR/LF 抛 IAE。

## 2. Java 用户如何使用
```java
Headers h = new Headers();
h.add("X-Test", "a");
h.add("X-Test", "b");
String first = h.getFirst("x-test");   // "a"（大小写不敏感）
Headers fixed = Headers.of("Content-Type", "text/plain");
```
请求头（`exchange.getRequestHeaders()`）为不可变，响应头（`exchange.getResponseHeaders()`）为可变。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/Headers.java` 实现 normalize 与全部方法；`sun/net/httpserver/UnmodifiableHeaders.java` 是不可变子类。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/UnmodifiableHeadersTest.java` 覆盖不可变行为。
- JDK 测试：`BasicAuthenticatorRealm.java` 用 `Headers.of()` 构造空响应头传给 `HttpHandlers.of(200, Headers.of(), "foo")`。

## 4. 测试用例设计 + 自动化实现
- 白盒：具体类、实现 Map、关键方法齐全（`HeadersTest.testWhiteBox`）。
- 黑盒（`HeadersTest.testBlackBox`）：
  - add/get/getFirst/set 多值顺序与覆盖、size 按不同键计数。
  - 大小写不敏感：`getFirst("x-test")`、`set("x-test")` 覆盖、`remove("x-single")`。
  - put/putAll/clear/keySet/values/entrySet/replaceAll 组合。
  - equals/hashCode（大小写不敏感）、toString。
  - `of(String...)` 多值、奇元素 IAE、空、不可变（add/set 抛 UOE）；`of(Map)` 不可变 remove 抛 UOE。
  - `Headers(Map)` 拷贝构造独立可写。
  - null key/value NPE、CR/LF value IAE。
