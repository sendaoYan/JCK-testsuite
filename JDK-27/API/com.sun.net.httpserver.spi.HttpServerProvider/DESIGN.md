# com.sun.net.httpserver.spi.HttpServerProvider — 设计案

## 1. API 功能
- 抽象类，HTTP 服务器的服务提供者接口。
- 静态方法：`provider()`：返回系统默认 provider（懒加载，重复调用返回同一实例）。
- 抽象方法：`createHttpServer(InetSocketAddress, int)`、`createHttpsServer(InetSocketAddress, int)`。
- 受保护构造器（无 public 构造器）。

## 2. Java 用户如何使用
```java
HttpServerProvider p = HttpServerProvider.provider();
HttpServer s = p.createHttpServer(new InetSocketAddress("127.0.0.1", 0), 0);
```
`HttpServer.create(...)` 与 `HttpsServer.create(...)` 内部即委托给 `provider()`。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/sun/net/httpserver/DefaultHttpServerProvider.java` 是默认实现。
- JDK 源码：`HttpServer.java`/`HttpsServer.java` 的工厂调用 `provider.createHttpServer/createHttpsServer`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/HttpServerProviderTest.java` 覆盖 `provider()` 非空。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、`provider/createHttpServer/createHttpsServer`、无 public 构造器（`HttpServerProviderTest.testWhiteBox`）。
- 黑盒（`HttpServerProviderTest.testBlackBox`）：
  - `provider()` 非空、重复调用同一实例。
  - `createHttpServer(ephemeral)` 绑定 ephemeral 端口，finally stop。
  - `createHttpsServer(ephemeral)` 绑定 ephemeral 端口（不做 TLS 握手），finally stop。
