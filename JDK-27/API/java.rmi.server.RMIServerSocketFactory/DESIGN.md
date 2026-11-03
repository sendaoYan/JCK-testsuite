# java.rmi.server.RMIServerSocketFactory 行为测试设计

## 1. API 功能
- 接口，单一抽象方法：`ServerSocket createServerSocket(int port) throws IOException`。
- 函数式接口（可用 lambda 实现）。
- 用于 RMI 服务端自定义 server socket 创建。

## 2. Java 用户如何使用
- `RMIServerSocketFactory ssf = (port) -> new ServerSocket(port);`。
- 与 `RMIClientSocketFactory` 成对传入导出/注册表 API。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RMIServerSocketFactory.java`：单方法接口。
- JDK 测试 `test/jdk/java/rmi/testlibrary/TestSocketFactory.java`：实现该接口。
- `javax.rmi.ssl.SslRMIServerSocketFactory` 是标准实现。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、单抽象方法 `createServerSocket(int)`。
- 黑盒：lambda 实例化 + 哨兵返回；匿名实现记录 port 入参验证。
- 实现见 `RMIServerSocketFactoryTest.java`。
