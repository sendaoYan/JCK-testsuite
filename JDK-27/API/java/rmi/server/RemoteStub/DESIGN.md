# java.rmi.server.RemoteStub 行为测试设计

## 1. API 功能
- 抽象类，`extends RemoteObject`；受保护构造器 `RemoteStub()`/`RemoteStub(RemoteRef)`。
- 静态方法 `protected static void setRef(RemoteStub, RemoteRef)`：始终抛 `UnsupportedOperationException`（弃用）。
- 是 JDK1.1 预生成 stub 的基类，现代用动态代理替代。

## 2. Java 用户如何使用
- 历史遗留：`RemoteStub` 子类通过 `RemoteStub(RemoteRef)` 构造注入引用。
- `setRef` 已废弃且不可用，需改用构造器注入。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteStub.java`：`setRef` 无条件抛 UnsupportedOperationException。
- JDK 测试 `test/jdk/java/rmi/server/RemoteObject/toStub/ToStub.java`：覆盖 stub 识别。
- 现代 RMI 用 `RemoteObjectInvocationHandler` 动态代理替代。

## 4. 测试用例设计 + 自动化实现
- 白盒：public abstract、父类 RemoteObject、`@Deprecated`。
- 黑盒：无参构造 getRef null；有参构造 getRef/hashCode/toString 委托；`setRef` 抛 UnsupportedOperationException 且不改 ref。
- 实现见 `RemoteStubTest.java`。
