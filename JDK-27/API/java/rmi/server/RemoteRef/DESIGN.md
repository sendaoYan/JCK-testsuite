# java.rmi.server.RemoteRef 行为测试设计

## 1. API 功能
- 接口，`extends Externalizable`，常量 `packagePrefix = "sun.rmi.server"`、`serialVersionUID`。
- 方法：`invoke(Remote, Method, Object[], long)`、`newCall(RemoteObject, Operation[], int, long)`（弃用）、`invoke(RemoteCall)`（弃用）、`done(RemoteCall)`（弃用）、`getRefClass(ObjectOutput)`、`remoteHashCode()`、`remoteEquals(RemoteRef)`、`remoteToString()`。

## 2. Java 用户如何使用
- `RemoteObject` 把 `hashCode`/`equals`/`toString` 委托给 `ref.remoteHashCode()`/`remoteEquals`/`remoteToString`。
- 远程调用的核心是 `invoke(Remote, Method, Object[], long)`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteObject.java`：`hashCode()` 调用 `ref.remoteHashCode()`，`toString()` 调用 `ref.remoteToString()`。
- JDK 源码 `RemoteObjectInvocationHandler.java`：`invokeRemoteMethod` 调用 `ref.invoke(...)`。
- JDK 测试 `test/jdk/java/rmi/server/RemoteObject/toStub/ToStub.java` 覆盖 stub/ref 交互。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、extends Externalizable、`packagePrefix` 值。
- 黑盒：匿名实现全部方法；`remoteHashCode`/`remoteToString`/`remoteEquals`；`getRefClass`；`invoke` 委托返回；`newCall`/`done` 可调用。
- 实现见 `RemoteRefTest.java`。
