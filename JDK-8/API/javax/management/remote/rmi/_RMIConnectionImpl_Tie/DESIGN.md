# _RMIConnectionImpl_Tie 测试设计（仅 JDK-8）

## 1. API 功能
`javax.management.remote.rmi._RMIConnectionImpl_Tie` 是 rmic `-iiop` 为 `RMIConnectionImpl` 生成的 CORBA Tie（连接级骨架）。**JDK-27 已删除**。

- 类层级：extends `org.omg.CORBA_2_3.portable.ObjectImpl` implements `javax.rmi.CORBA.Tie`（接口，extends InvokeHandler）；public 无参构造器；非 Serializable。
- `_ids()` 恰返回 `["RMI:javax.management.remote.rmi.RMIConnection:0000000000000000"]`。
- `setTarget` 强制 cast 到 `RMIConnectionImpl`（错误类型 → CCE `... cannot be cast to javax.management.remote.rmi.RMIConnectionImpl`）；`setTarget(null)` 容忍。
- 无 delegate 时：`orb()`/`deactivate()` → `org.omg.CORBA.BAD_OPERATION`（deactivate 消息 `"The delegate has not been set!"`）；`thisObject()` toString 以 `"no delegate set"` 结尾。

## 2. Java 用户如何使用
```java
_RMIConnectionImpl_Tie tie = new _RMIConnectionImpl_Tie();
tie.setTarget(rmiConnectionImpl);   // IIOP 导出时由 PortableRemoteObject 装配
```

## 3. 开源/源码实际用法调研
- rmic `-iiop` 生成物；`RMIIIOPServerImpl.makeClient` → `RMIConnectionImpl` + `exportObject` 装配本 Tie。
- 与 `_RMIServerImpl_Tie` 完全对称，仅目标类型/repo id 不同。
来源：javap + JDK-8 jtreg 实证。

## 4. 测试用例设计
白盒：具体类、ObjectImpl 层级、Tie/InvokeHandler 视图、方法集、非 Serializable。
黑盒深化：
- 直构造：`_ids()` 精确单元素数组（RMIConnection repo id）；`getTarget()` 初始 null；`setTarget(null)` 容忍。
- `setTarget(new Remote(){})` → CCE 精确消息。
- `setTarget(真实 RMIConnectionImpl)`/`getTarget()` 同一性往返——构造 `RMIConnectionImpl` 需宿主 `TinyServer`（覆写 clientClosed）**且必须先 setMBeanServer**（构造器内 `mbeanServer.getClassLoaderRepository()`，否则无消息 NPE）。
- 无 delegate：`orb()` → BAD_OPERATION；`deactivate()` → BAD_OPERATION 精确消息；`thisObject()` toString endsWith `"no delegate set"`。

## 5. 深化（2026-11）与版本差异
- **目录差集**：仅 JDK-8 侧存在（8↔27 差集 5 类之一）。
- 构造依赖坑：RMIConnectionImpl 直构造要求宿主 impl 挂 MBeanServer（无消息 NPE，栈帧在 doPrivileged 内）——已在测试注释说明。
- 与 `_RMIServerImpl_Tie` 对称；无 `_invoke` 调用（需 CORBA 流）。
