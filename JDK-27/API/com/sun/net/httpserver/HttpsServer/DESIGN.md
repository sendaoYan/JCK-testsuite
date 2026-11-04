# com.sun.net.httpserver.HttpsServer — 设计案

## 1. API 功能
- 抽象类，HTTPS 服务器，继承 `HttpServer`。
- 工厂：`create()`（未绑定）、`create(InetSocketAddress, int)`（绑定）、`create(InetSocketAddress, int, String, HttpHandler, Filter...)`（绑定+初始上下文）。
- 方法：`setHttpsConfigurator(HttpsConfigurator)`（null 抛 NPE）、`getHttpsConfigurator()`（默认 null）。
- 继承 `bind/start/stop/createContext/removeContext/getAddress/setExecutor/getExecutor`。

## 2. Java 用户如何使用
```java
HttpsServer s = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
s.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
s.createContext("/", handler);
s.start();
```
本组不做真实 TLS 握手：只验证创建、绑定、configurator 的 set/get 与校验。

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/HttpsServer.java` 工厂委托 `HttpServerProvider.createHttpsServer`。
- JDK 源码：`sun/net/httpserver/HttpsServerImpl.java` 的 `setHttpsConfigurator` 拒绝 null（NPE）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/TestHttpsServer.java` 等使用 `HttpsServer.create` + `setHttpsConfigurator`。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、继承 HttpServer、`create/setHttpsConfigurator/getHttpsConfigurator`（`HttpsServerTest.testWhiteBox`）。
- 黑盒（`HttpsServerTest.testBlackBox`）：
  - `create(ephemeral)` 立即绑定 ephemeral 端口；configurator 默认 null、set/get、null 抛 NPE。
  - `create()` 未绑定 → `bind` 后 ephemeral。
  - finally stop(0) 释放端口，不发起真实 TLS 请求。
