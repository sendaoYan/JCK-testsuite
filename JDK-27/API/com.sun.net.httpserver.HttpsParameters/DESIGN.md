# com.sun.net.httpserver.HttpsParameters — 设计案

## 1. API 功能
- 抽象类，表示每个 HTTPS 连接的协商参数。
- 抽象方法：`getHttpsConfigurator()`、`getClientAddress()`、`setSSLParameters(SSLParameters)`。
- 具体方法（旧式，JDK 23 起标记 `@Deprecated`）：
  - `getCipherSuites()/setCipherSuites(String[])`（get 返回防御性拷贝、set 克隆输入）
  - `getProtocols()/setProtocols(String[])`
  - `getWantClientAuth()/setWantClientAuth(boolean)`（设置 want 会清 need）
  - `getNeedClientAuth()/setNeedClientAuth(boolean)`（设置 need 会清 want）

## 2. Java 用户如何使用
`HttpsConfigurator.configure` 中：
```java
public void configure(HttpsParameters params) {
    params.setSSLParameters(getSSLContext().getDefaultSSLParameters());
}
```

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/HttpsParameters.java` 实现 cipherSuites/protocols 的 clone 与 want/need 互斥。
- JDK 源码：`HttpsConfigurator.configure` 默认实现调用 `params.setSSLParameters(...)`。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/HttpsParametersClientAuthTest.java` 覆盖 client auth 参数。

## 4. 测试用例设计 + 自动化实现
- 白盒：抽象类、11 个方法齐全（`HttpsParametersTest.testWhiteBox`）。
- 黑盒（`HttpsParametersTest.testBlackBox`）：
  - 匿名实现三个抽象方法并回读 `getHttpsConfigurator/getClientAddress`。
  - cipherSuites：默认 null、set/get、get 防御性拷贝、set 克隆输入、null 清空。
  - protocols：同语义。
  - want/need client auth 默认 false、互斥清对方。
  - `setSSLParameters` 记录分发。
