# _RMIServer_Stub 测试设计（仅 JDK-8）

## 1. API 功能
`javax.management.remote.rmi._RMIServer_Stub` 是 rmic `-iiop` 为 `RMIServer` 接口生成的 CORBA 客户端 stub（服务器级）。**JDK-27 已删除**。

- 类层级：extends `javax.rmi.CORBA.Stub`，implements `RMIServer`；public 无参构造器；Serializable，`serialVersionUID == 5043125727644346603`。
- `_ids()` 恰返回 `["RMI:javax.management.remote.rmi.RMIServer:0000000000000000"]`。
- 未连接（无 delegate）行为矩阵：
  - `equals(self)` → true（不触 delegate）；`toString()` → `"..._RMIServer_Stub: no delegate set"`（不抛）。
  - `hashCode()`、`equals(otherStub)` → `org.omg.CORBA.BAD_OPERATION`（SystemException 直抛，消息含 `"The delegate has not been set!"`）。
  - 远程方法 `getVersion()`/`newClient(null)` → `java.rmi.RemoteException` 包装 BAD_OPERATION（消息含 `"CORBA BAD_OPERATION"`，`getCause()` 即 BAD_OPERATION）。

## 2. Java 用户如何使用
```java
_RMIServer_Stub stub = (_RMIServer_Stub) PortableRemoteObject.narrow(ref, RMIServer.class);
String v = stub.getVersion();
```
经 `javax.rmi.PortableRemoteObject.narrow` 从 CORBA 引用获得。

## 3. 开源/源码实际用法调研
- rmic `-iiop` 生成物；`javax.rmi.CORBA.Stub` 基类的 connect/equals/hashCode 均依赖 delegate。
- `connect(null)` 未连接时会打 CORBA WARNING 日志（BAD_PARAM "no Tie was found"）——测试避免调用以免噪声。
来源：javap + JDK-8 jtreg/scratch 实证。

## 4. 测试用例设计
白盒：具体类、superclass `javax.rmi.CORBA.Stub`、RMIServer 视图、方法集（_ids/getVersion/newClient）。
黑盒深化：
- 直构造：`_ids()` 精确单元素数组；svuid 精确值。
- 未连接身份方法：`equals(self)` true、`toString()` endsWith `"no delegate set"`。
- 未连接负例：`hashCode()` → BAD_OPERATION；`equals(new _RMIServer_Stub())` → BAD_OPERATION；`getVersion()`/`newClient(null)` → RemoteException（消息含 BAD_OPERATION 且 cause 为 BAD_OPERATION）。

## 5. 深化（2026-11）与版本差异
- **目录差集**：仅 JDK-8 侧存在（8↔27 差集 5 类之一）；JDK-27 的 `rmi+iiop` 协议在连接器服务器构造期即被拒。
- 与 JRMP 版 `RMIServerImpl_Stub` 的关键差异：本类无 `(RemoteRef)` 构造器（CORBA stub 无 RemoteRef），失败模式是 RemoteException 包 SystemException 而非 NoSuchObjectException。
- `connect(null)` 不测（噪声日志）。
