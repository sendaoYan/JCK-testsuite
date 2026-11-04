# java.rmi.server.LoaderHandler 行为测试设计

## 1. API 功能
- 接口（`@Deprecated`），常量 `packagePrefix = "sun.rmi.server"`。
- 方法：`loadClass(String name)`、`loadClass(URL codebase, String name)`、`getSecurityContext(ClassLoader)`。
- 为 JDK1.1 的类加载/安全上下文抽象。

## 2. Java 用户如何使用
- 实现该接口以接管 RMI 的类加载与安全上下文解析。
- 现代 RMI 由 `RMIClassLoader`/`RMIClassLoaderSpi` 取代，此接口仅存留兼容。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/LoaderHandler.java`：`packagePrefix = "sun.rmi.server"`。
- JDK 源码 `sun.rmi.server.LoaderHandler` 曾是默认实现（与接口同名，位于内部包）。
- 该接口与 `RemoteRef.packagePrefix` 共享同一前缀常量。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、`@Deprecated`、`packagePrefix` 值。
- 黑盒：匿名实现三方法；`loadClass` 委托 `Class.forName`；`loadClass(URL, name)` 与 `getSecurityContext` 哨兵返回。
- 实现见 `LoaderHandlerTest.java`。
