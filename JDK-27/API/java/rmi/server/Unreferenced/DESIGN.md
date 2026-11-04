# java.rmi.server.Unreferenced 行为测试设计

## 1. API 功能
- 接口，单一抽象方法：`void unreferenced()`。
- 函数式接口（可用 lambda 实现）。
- 由远程对象实现：当该对象不再被任何客户端引用时回调（DGC）。

## 2. Java 用户如何使用
- 远程对象实现 `Unreferenced` 接口，在 `unreferenced()` 中释放资源。
- 由 RMI 分布式垃圾收集（DGC）在引用计数归零时调用。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/server/Unreferenced/unreferencedContext/UnreferencedContext.java`：导出实现 `Unreferenced` 的对象并验证回调上下文。
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/Unreferenced.java`：单方法接口。
- DGC 内部 `sun.rmi.transport.DGCClient` 管理引用计数。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、单抽象方法 `unreferenced()`。
- 黑盒：lambda 实例化 + 调用无异常；匿名实现记录回调被触发。
- 实现见 `UnreferencedTest.java`。
