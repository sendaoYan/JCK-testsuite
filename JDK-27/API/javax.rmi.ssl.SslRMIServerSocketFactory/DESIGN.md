# javax.rmi.ssl.SslRMIServerSocketFactory 行为测试设计

## 1. API 功能
- 类，`implements RMIServerSocketFactory`。
- 构造器：无参、`(String[] ciphers, String[] protocols, boolean needClientAuth)`、`(SSLContext, String[], String[], boolean)`。
- 方法：`getEnabledCipherSuites()`/`getEnabledProtocols()`（返回防御性拷贝）、`getNeedClientAuth()`、`createServerSocket(int)`、`equals`/`hashCode`。

## 2. Java 用户如何使用
- `new SslRMIServerSocketFactory()` 使用默认 SSL 配置；三参构造指定 cipher/protocol/needClientAuth。
- 不支持的 cipher/protocol 在构造时抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/javax/rmi/ssl/SslRMIServerSocketFactory.java`：getters 返回 `clone()`；构造时校验 cipher/protocol。
- JDK 测试 `test/jdk/javax/rmi/ssl/SocketFactoryTest.java`：自定义子类覆盖构造器。
- JDK 测试 `test/jdk/javax/rmi/ssl/SSLSocketParametersTest.java`：显式 cipher/protocol/needClientAuth 场景。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、implements RMIServerSocketFactory、`createServerSocket(int)` 签名。
- 黑盒：默认 getters null/false；显式 getters 值与防御性拷贝；equals/hashCode（同配置等/异配置不等/不等于 null）；不支持 cipher 抛 IllegalArgumentException；`createServerSocket(0)` 绑定临时端口后关闭。
- 实现见 `SslRMIServerSocketFactoryTest.java`。
