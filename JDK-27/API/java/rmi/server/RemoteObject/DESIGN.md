# java.rmi.server.RemoteObject 行为测试设计

## 1. API 功能
- 抽象类，`implements Remote, Serializable`；受保护构造器 `RemoteObject()`/`RemoteObject(RemoteRef)`。
- 方法：`getRef()`、`static Remote toStub(Remote)`、`hashCode()`、`equals(Object)`、`toString()`。
- 语义：`hashCode`/`equals`/`toString` 在 `ref != null` 时委托给 ref。

## 2. Java 用户如何使用
- 远程对象基类；子类通过 `super(ref)` 注入远程引用。
- `RemoteObject.toStub(obj)` 取已导出对象的 stub（未导出抛 NoSuchObjectException，stub/proxy 直接返回自身）。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteObject.java`：`hashCode()` 委托 `ref.remoteHashCode()`。
- JDK 测试 `test/jdk/java/rmi/server/RemoteObject/toStub/ToStub.java`：覆盖 `toStub` 行为。
- `UnicastRemoteObject`、`RemoteStub`、`RemoteObjectInvocationHandler` 均继承 RemoteObject。

## 4. 测试用例设计 + 自动化实现
- 白盒：public abstract、implements Remote/Serializable、父类 Object。
- 黑盒：无 ref 实例 getRef null/equals/toString；有 ref 实例 getRef/hashCode/toString 委托；`toStub(unexported)` 抛 NoSuchObjectException；`toStub(RemoteStub)` 返回自身。
- 实现见 `RemoteObjectTest.java`。
