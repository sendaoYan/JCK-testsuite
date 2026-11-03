# com.sun.net.httpserver.HttpsConfigurator — 设计案

## 1. API 功能
- 具体类，为每个入站 HTTPS 连接配置 SSL 参数。
- 构造器：`HttpsConfigurator(SSLContext)`（null 抛 NPE）。
- 方法：`getSSLContext()`；`void configure(HttpsParameters)`（默认实现调用 `params.setSSLParameters(getSSLContext().getDefaultSSLParameters())`，可被覆写）。

## 2. Java 用户如何使用
```java
SSLContext ctx = SSLContext.getDefault();
HttpsServer server = HttpsServer.create(addr, 0);
server.setHttpsConfigurator(new HttpsConfigurator(ctx) {
    public void configure(HttpsParameters params) {
        params.setSSLParameters(getSSLContext().getDefaultSSLParameters());
    }
});
```

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/HttpsConfigurator.java` 的默认 `configure` 用 `getSSLContext().getDefaultSSLParameters()`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/HttpsParametersClientAuthTest.java` 使用 `HttpsConfigurator` 配置 client auth。

## 4. 测试用例设计 + 自动化实现
- 白盒：具体类、`(SSLContext)` 构造器、`getSSLContext/configure`（`HttpsConfiguratorTest.testWhiteBox`）。
- 黑盒（`HttpsConfiguratorTest.testBlackBox`）：
  - 默认 context 与 `SSLContext.getInstance("TLS")` 构造回读。
  - null SSLContext 抛 NPE。
  - 覆写 `configure` 用匿名 `HttpsParameters` 断言分发到覆写方法（不做真实 TLS 握手）。
