# java.rmi.server.SkeletonMismatchException 行为测试设计

## 1. API 功能
- 构造器：`SkeletonMismatchException(String s)`（仅单参）。
- 继承：`extends java.rmi.RemoteException`。
- 语义：JDK1.1 skeleton 分派时方法 hash 不匹配抛出。

## 2. Java 用户如何使用
- 历史遗留；现代 RMI 用动态代理/序列化 stub，不直接构造该异常。
- `new SkeletonMismatchException("msg")` 后 `getMessage()` 返回 `"msg"`，`getCause()` 为 null。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/SkeletonMismatchException.java`：单构造器，仅存留于兼容层。
- 该类已随 JDK1.2 的 stub/skeleton 机制淘汰，`Skeleton` 接口标记 `@Deprecated`。
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java` 覆盖 RemoteException 家族异常链通用行为。

## 4. 测试用例设计 + 自动化实现
- 白盒：父类为 `RemoteException`、public、具体类。
- 黑盒：单参构造 message；`getCause()` 与 `detail` 为 null；`initCause` 抛 `IllegalStateException`。
- 实现见 `SkeletonMismatchExceptionTest.java`。
