# java.rmi.server.RMISocketFactory 行为测试设计

## 1. API 功能
- 抽象类，`implements RMIClientSocketFactory, RMIServerSocketFactory`。
- 抽象方法：`createSocket(String,int)`、`createServerSocket(int)`。
- 静态方法：`setSocketFactory(RMISocketFactory)`、`getSocketFactory()`、`getDefaultSocketFactory()`、`setFailureHandler(RMIFailureHandler)`、`getFailureHandler()`。

## 2. Java 用户如何使用
- `RMISocketFactory.getDefaultSocketFactory()` 取默认工厂；`getSocketFactory()` 返回自定义工厂（默认 null）。
- `setFailureHandler(h)` 注册失败回调；子类覆盖两个抽象方法实现自定义 socket。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RMISocketFactory.java`：静态工厂状态与 default factory 懒初始化。
- JDK 测试 `test/jdk/java/rmi/testlibrary/TestSocketFactory.java`：自定义 socket 工厂。
- RMI 传输层通过该工厂创建客户端/服务端 socket。

## 4. 测试用例设计 + 自动化实现
- 白盒：public abstract、实现 RMIClientSocketFactory/RMIServerSocketFactory、父类 Object。
- 黑盒：默认工厂非 null；`getSocketFactory` null；`getFailureHandler` null；匿名子类两方法哨兵返回 + instanceof 两接口；`setFailureHandler`/`getFailureHandler` 往返（用后恢复 null）。
- 实现见 `RMISocketFactoryTest.java`。
