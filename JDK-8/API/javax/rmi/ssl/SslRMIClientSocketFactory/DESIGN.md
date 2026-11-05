# javax.rmi.ssl.SslRMIClientSocketFactory 测试设计（JDK-8 侧）

## 1. API 功能
RMI 客户端 SSL 套接字工厂（实现 `RMIClientSocketFactory`，Serializable，无配置状态）。
- 方法：`createSocket(String host, int port)`、`equals`/`hashCode`（无状态 → 全体实例值相等）。

## 2. Java 用户如何使用
```java
Registry r = LocateRegistry.getRegistry("host", 0, new SslRMIClientSocketFactory());
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/ssl/`：getter 防御拷贝、构造期 cipher/protocol 校验的权威实现。
- JDK 测试 `test/javax/rmi/ssl/SocketFactoryTest.java`、`SSLSocketParametersTest.java`。
## 4. 测试用例设计 + 自动化实现
- 白盒：public、实现 RMIClientSocketFactory + Serializable、两方法形态。
- 黑盒：默认两实例 equals 且 hashCode 相等（无状态值语义）；null/异类型不等；
  序列化往返后 equals/hashCode 保持；`createSocket(null, 0)` 与连接关闭端口 → IOException 精确抛出。
- 实现见 `SslRMIClientSocketFactoryTest.java`。
