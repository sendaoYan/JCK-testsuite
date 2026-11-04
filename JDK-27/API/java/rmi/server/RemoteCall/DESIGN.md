# java.rmi.server.RemoteCall 行为测试设计

## 1. API 功能
- 接口（`@Deprecated`），抽象方法：`getOutputStream()`、`releaseOutputStream()`、`getInputStream()`、`releaseInputStream()`、`getResultStream(boolean)`、`executeCall()`、`done()`。
- 表示一次远程调用（JDK1.1 风格 stub/skeleton 使用）。

## 2. Java 用户如何使用
- 历史遗留：旧式 `RemoteRef.newCall(...)` 返回 `RemoteCall`，随后读写参数、执行调用、释放流。
- 现代 RMI 用 `RemoteRef.invoke(Remote, Method, Object[], long)` 替代。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteCall.java`：接口 + `@Deprecated`。
- JDK 源码 `RemoteRef.java`：`newCall`/`invoke(RemoteCall)`/`done(RemoteCall)` 与之配合。
- 该类与 `Operation`、`Skeleton` 同属 JDK1.1 遗留协议层。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、`@Deprecated`、七个方法名齐全。
- 黑盒：匿名实现七方法；哨兵返回值；`executeCall` 回调被触发；`release*`/`done` 可调用。
- 实现见 `RemoteCallTest.java`。
