# java.rmi.server.RMIClientSocketFactory 行为测试设计

## 1. API 功能
- 接口，单一抽象方法：`Socket createSocket(String host, int port) throws IOException`。
- 函数式接口（可用 lambda 实现）。
- 用于 RMI 客户端自定义 socket 创建（如 SSL、超时）。

## 2. Java 用户如何使用
- `RMIClientSocketFactory csf = (host, port) -> new Socket(host, port);`。
- 通过 `UnicastRemoteObject.exportObject(obj, port, csf, ssf)` 或 `LocateRegistry` 传入。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RMIClientSocketFactory.java`：单方法接口。
- JDK 测试 `test/jdk/java/rmi/testlibrary/TestSocketFactory.java`：实现 RMIClientSocketFactory/RMIServerSocketFactory 用于测试。
- `javax.rmi.ssl.SslRMIClientSocketFactory` 是标准实现。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、单抽象方法 `createSocket(String,int)`。
- 黑盒：lambda 实例化 + 哨兵返回；匿名实现记录 host/port 入参验证。
- 实现见 `RMIClientSocketFactoryTest.java`。
