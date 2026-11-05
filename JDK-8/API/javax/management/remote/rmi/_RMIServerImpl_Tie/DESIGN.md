# _RMIServerImpl_Tie 测试设计（仅 JDK-8）

## 1. API 功能
`javax.management.remote.rmi._RMIServerImpl_Tie` 是 rmic `-iiop` 为 `RMIServerImpl` 生成的 CORBA Tie（服务端骨架）。**JDK-27 已删除**（javax.rmi.CORBA 随 CORBA 移除）。

- 类层级：extends `org.omg.CORBA_2_3.portable.ObjectImpl` implements `javax.rmi.CORBA.Tie`（后者是 extends `org.omg.CORBA.portable.InvokeHandler` 的**接口**，不 extends Remote）。
- public 无参构造器可直接 new；非 Serializable；方法 `_ids/_invoke/deactivate/orb/setTarget/getTarget/thisObject/...`。
- `_ids()` 恰返回 `["RMI:javax.management.remote.rmi.RMIServer:0000000000000000"]`。
- `setTarget` 强制 cast 到 `RMIServerImpl`（错误类型 → ClassCastException，消息 `... cannot be cast to javax.management.remote.rmi.RMIServerImpl`）；`setTarget(null)` 容忍、`getTarget` 初始 null。
- 无 CORBA delegate 时：`orb()`/`deactivate()` → `org.omg.CORBA.BAD_OPERATION`（deactivate 消息 `"The delegate has not been set!"`）；`thisObject()` 不抛，toString 以 `"no delegate set"` 结尾。

## 2. Java 用户如何使用
```java
_RMIServerImpl_Tie tie = new _RMIServerImpl_Tie();
tie.setTarget(rmiServerImpl);            // POA 绑定后经 _invoke 分发调用
org.omg.CORBA.Object ref = tie.thisObject(); // 经 ORB 变为可分发引用
```

## 3. 开源/源码实际用法调研
- rmic `-iiop` 生成物：`_invoke(String method, InputStream, ResponseHandler)` 按 repository id 分发；`setTarget` 持有被代理的 `RMIServerImpl`。
- JDK 内部由 `javax.management.remote.rmi.RMIIIOPServerImpl.export` 经 `javax.rmi.CORBA.Util.registerTarget` 装配。
来源：javap + JDK-8 jtreg 实证（/tmp scratch 直测）。

## 4. 测试用例设计
白盒：具体类、superclass/ObjectImpl 层级、`javax.rmi.CORBA.Tie`/`InvokeHandler` 视图、方法集（_ids/_invoke/deactivate）、非 Serializable。
黑盒深化：
- 直构造：`_ids()` 精确单元素数组；`getTarget()` 初始 null；`setTarget(null)` 容忍。
- `setTarget(new Remote(){})` → CCE 精确消息（endsWith 断言）。
- `setTarget(TinyServer)`/`getTarget()` 同一性往返。
- 无 delegate：`orb()` → BAD_OPERATION；`deactivate()` → BAD_OPERATION `"The delegate has been set!"` 之精确串 `"The delegate has not been set!"`；`thisObject()` 非 null 且 toString endsWith `"no delegate set"`。
- **绝不调用 `_invoke`/ORB 连接**（需要 CORBA 流且拉起 ORB 线程）。

## 5. 深化（2026-11）与版本差异
- **目录差集**：本类仅 JDK-8 侧存在（与 `_RMIConnectionImpl_Tie`、`_RMIServer_Stub`、`_RMIConnection_Stub`、`RMIIIOPServerImpl` 同为 8↔27 差集的 5 个）。
- 层级勘误：`javax.rmi.CORBA.Tie` 在 JDK-8 是接口（extends InvokeHandler），并非某些文档所述的 abstract class——测试最初按 Remote 断言失败后修正为 InvokeHandler 视图。
- 兄弟文件：`_RMIConnectionImpl_Tie`（setTarget cast 到 RMIConnectionImpl，其余对称）。
