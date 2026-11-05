# RMIIIOPServerImpl 测试设计（仅 JDK-8）

## 1. API 功能
`javax.management.remote.rmi.RMIIIOPServerImpl`（extends `RMIServerImpl`）是 IIOP 传输的连接器服务器实现。**JDK-27 已删除本类**（CORBA/Java EE 模块整体移除）。

- public `(Map env)` 构造器；protected 钩子 `export/makeClient/closeClient/closeServer/getProtocol` + 继承的 `toStub`；非 Serializable。
- `getVersion()` = `"1.0 java_runtime_" + 版本`；未挂 MBeanServer 时 `newClient` → ISE `"Not attached to an MBean server"`。
- 未导出行为：`toStub()` → `NoSuchObjectException("object not exported")`（小写 o、无句号）；`close()` → `NoSuchObjectException("Object not exported.")`（大写 O、有句号——与 JRMP 实现的消息差异，双侧实测锁定）。

## 2. Java 用户如何使用
```java
RMIServerImpl impl = new RMIIIOPServerImpl(env);
RMIConnectorServer cs = new RMIConnectorServer(
        new JMXServiceURL("service:jmx:rmi+iiop://127.0.0.1"), env, impl, mbs);
cs.start();   // CORBA POA 导出
```

## 3. 开源/源码实际用法调研
- JDK 源 `RMIIIOPServerImpl.java`（jdk8u-dev）：`export` 经 `javax.rmi.CORBA.Util.registerTarget`+Tie 绑定；`makeClient` 构造 `RMIConnectionImpl` 后 `portableRemoteObject.exportObject`。
- 关键风险：IIOP 导出拉起 ORB 监听线程（非守护），进程不退出。
来源：JDK 源码 + JDK-8 jtreg 实证（scratch 实测 start→JVM 挂死 EXIT=124）。

## 4. 测试用例设计
白盒：具体类、superclass=`RMIServerImpl`、protected 钩子方法集、public `(Map)` ctor、非 Serializable、`toStub` 存在。
黑盒深化：
- 直构造：`getVersion` 前缀 `"1.0 "`+含 `java_runtime_`、`getMBeanServer()` 初始 null→set/get 往返、`setDefaultClassLoader` 往返、null env 容忍。
- 未导出负例：`toStub` → NoSuchObjectException 精确消息 `"object not exported"`；`close` → `"Object not exported."`；无 MBS `newClient` → ISE 精确消息。
- 子类化（SubIIOP 覆写全部钩子为 no-op）：构造成功、`getProtocol()` 覆写生效、`close()`（closeServer no-op）成功且幂等——证明类可扩展。
- **绝不 `start()`/`export()`**：ORB 非守护线程会挂死测试 JVM。

## 5. 深化（2026-11）与版本差异
- **目录差集**：本类仅存在于 JDK-8 侧（`JDK-8/API/javax/management/remote/rmi/RMIIIOPServerImpl/`）；JDK-27 无对应目录。JDK-27 侧 `rmi+iiop` URL 在 `RMIConnectorServer` 构造期即 `MalformedURLException("Invalid protocol type: iiop")`。
- 消息大小写 quirk：`toStub`（"object not exported"）vs `close`（"Object not exported."）——同类两条不同精确串，均已断言。
- 兄弟类对照：`RMIJRMPServerImpl` 的 `toStub`/`close` 未导出消息均为 `"object not exported"`。
