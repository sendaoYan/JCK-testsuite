# javax.rmi.ssl.SslRMIServerSocketFactory 测试设计（JDK-8 侧）

## 1. API 功能
RMI 服务端 SSL 套接字工厂（实现 `RMIServerSocketFactory`，Serializable）。
- 构造器：无参（默认 SSL 参数）、`(String[] ciphers, String[] protocols, boolean needClientAuth)`、
  `(SSLContext, String[], String[], boolean)`。
- 读取：`getEnabledCipherSuites/getEnabledProtocols/getNeedClientAuth`（均防御拷贝）。
- 行为：`createServerSocket(int)` 产出绑定的 SSLServerSocket；equals/hashCode 按配置值语义。

## 2. Java 用户如何使用
```java
SslRMIServerSocketFactory f = new SslRMIServerSocketFactory(
        new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA"}, new String[]{"TLSv1.2"}, true);
LocateRegistry.createRegistry(0, new SslRMIClientSocketFactory(), f);
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/ssl/`：getter 防御拷贝、构造期 cipher/protocol 校验的权威实现。
- JDK 测试 `test/javax/rmi/ssl/SocketFactoryTest.java`、`SSLSocketParametersTest.java`。
## 4. 测试用例设计 + 自动化实现
- 白盒：public final、两接口、构造器与方法形态。
- 黑盒（防御拷贝闭环）：显式构造的 cipher/protocol 数组传后改外层，getter 不变；getter 两次返回不同数组实例；
  null 数组构造 → null getter（“未配置”语义）；needClientAuth 往返；SSLContext 变体的组合；
  equals/hashCode（同配置等、异配置不等、null/异类型 false）；
  负例（cipher 数组含 null 元素 / 不支持的 cipher / 不支持的 protocol → IllegalArgumentException 精确抛出）；
  `createServerSocket(0)` 绑定临时端口、非 null、可关闭。
- 实现见 `SslRMIServerSocketFactoryTest.java`。
