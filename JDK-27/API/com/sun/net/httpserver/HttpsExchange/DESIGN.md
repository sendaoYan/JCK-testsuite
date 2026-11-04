# com.sun.net.httpserver.HttpsExchange — 设计案

## 1. API 功能
- 抽象类，表示 HTTPS 交换，继承 `HttpExchange`。
- 新增方法：`abstract SSLSession getSSLSession()`。
- 受保护构造器 `protected HttpsExchange()`。
- 由于 TLS 握手复杂，本组不做真实 TLS 请求，只做白盒结构 + 匿名实现/构造断言。

## 2. Java 用户如何使用
HTTPS handler 内：
```java
SSLSession ssl = ((HttpsExchange) exchange).getSSLSession();
```
仅在真实 `HttpsServer` 的 TLS 连接上由服务器注入实例；用户代码一般只在 handler/filter 中读取。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/sun/net/httpserver/HttpsExchangeImpl.java` 是实际实现。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/TestHttpsServer.java` 等 TLS 测试会经 `HttpsServer` 拿到 `HttpsExchange` 并调用 `getSSLSession()`。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、继承 HttpExchange、`getSSLSession` 方法（`HttpsExchangeTest.testWhiteBox`）。
- 黑盒（`HttpsExchangeTest.testBlackBox`）：匿名 `HttpsExchange` 实现全部抽象方法，`getSSLSession()` 返回 null 可调用；并断言 `instanceof HttpExchange`、`instanceof Request`（继承链/接口）。
