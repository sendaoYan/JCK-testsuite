# java.rmi.server.SkeletonNotFoundException 行为测试设计

## 1. API 功能
- 构造器：`SkeletonNotFoundException(String s)`、`SkeletonNotFoundException(String s, Exception ex)`。
- 继承：`extends java.rmi.RemoteException`。
- 语义：JDK1.1 时代找不到对应 skeleton 类时抛出。

## 2. Java 用户如何使用
- 历史遗留；现代 RMI 不使用 skeleton。
- 双参构造携带根因：`getCause()`/`detail` 指向 `ex`，`getMessage()` 前缀 + `nested exception` 标记。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/SkeletonNotFoundException.java`：继承 RemoteException，两构造器。
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java`：验证 `detail` 与 `getCause()` 指向同一 cause。
- `sun.rmi.server.*` 的旧式 skeleton 分派路径曾抛出此异常。

## 4. 测试用例设计 + 自动化实现
- 白盒：父类为 `RemoteException`、public、具体类。
- 黑盒：单参 message + cause null；双参 cause 同一性 + message 前缀 + `nested exception` + 含 cause 文本。
- 实现见 `SkeletonNotFoundExceptionTest.java`。
