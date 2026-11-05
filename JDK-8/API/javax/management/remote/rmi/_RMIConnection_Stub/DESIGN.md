# _RMIConnection_Stub 测试设计（仅 JDK-8）

## 1. API 功能
`javax.management.remote.rmi._RMIConnection_Stub` 是 rmic `-iiop` 为 `RMIConnection` 接口生成的 CORBA 客户端 stub（连接级）。**JDK-27 已删除**。

- 类层级：extends `javax.rmi.CORBA.Stub`，implements `RMIConnection`；public 无参构造器；Serializable，`serialVersionUID == 319376793193080821`。
- `_ids()` 恰返回 `["RMI:javax.management.remote.rmi.RMIConnection:0000000000000000"]`。
- 未连接行为矩阵与 `_RMIServer_Stub` 对称：`equals(self)`/`toString()` 可用；`hashCode()`/`equals(other)` → `org.omg.CORBA.BAD_OPERATION`；远程方法 `getConnectionId()`/`close()` → `java.rmi.RemoteException` 包 BAD_OPERATION。

## 2. Java 用户如何使用
```java
RMIConnection rc = (RMIConnection) PortableRemoteObject.narrow(
        serverStub.newClient(creds), RMIConnection.class);
rc.getAttribute(name, "Count", null);
```

## 3. 开源/源码实际用法调研
- rmic `-iiop` 生成物；与 JRMP 版 `RMIConnectionImpl_Stub` 同接口不同传输。
- 完整 27 连接方法皆经 delegate 编组；无 delegate 时全部包装为 RemoteException。
来源：javap + JDK-8 jtreg/scratch 实证。

## 4. 测试用例设计
白盒：具体类、superclass `javax.rmi.CORBA.Stub`、RMIConnection 视图、方法集（_ids/addNotificationListener(s) 等）、非 abstract。
黑盒深化：
- 直构造：`_ids()` 精确单元素数组（RMIConnection repo id）；svuid 精确值。
- 未连接身份方法：`equals(self)` true、`toString()` endsWith `"no delegate set"`。
- 未连接负例：`hashCode()` → BAD_OPERATION；`equals(other)` → BAD_OPERATION；`getConnectionId()`/`close()` → RemoteException（消息含 BAD_OPERATION，cause 为 BAD_OPERATION）。

## 5. 深化（2026-11）与版本差异
- **目录差集**：仅 JDK-8 侧存在（8↔27 差集 5 类之一）。
- svuid 与 `_RMIServer_Stub`（5043125727644346603）不同（319376793193080821）——两 stub 分别锁定。
- 不测 `connect(null)`（CORBA WARNING 日志噪声）。
