# RMIConnectionImpl_Stub 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnectionImpl_Stub` 是 rmic 为 `RMIConnectionImpl` 生成的 JRMP 客户端 stub（连接级）。

- final 类，extends `java.rmi.server.RemoteStub`，implements `RMIConnection`；public `(RemoteRef)` 构造器；`serialVersionUID == 2`；Serializable。
- 无 `getVersion`（那是服务器级 stub 的方法）；每个远程调用都经 `ref.invoke(...)`（反射式 RemoteRef 协议）编组。
- 真实对象由服务器侧 `RMIServerImpl.newClient` 返回，经 `RMIConnector` 内部代理驱动。

## 2. Java 用户如何使用
```java
RMIServer serverStub = (RMIServer) impl.toStub();
RMIConnection rc = serverStub.newClient(null);   // instanceof RMIConnectionImpl_Stub
Integer count = (Integer) rc.getAttribute(on, "Count", null);
rc.close();
```
用户一般通过 `JMXConnectorFactory` 间接获得；协议级测试直接持有本类。

## 3. 开源/源码实际用法调研
- rmic `-v1.2` 生成物：单一 `(RemoteRef)` 构造器、全部方法委托 `ref.invoke(Remote, Method, Object[], long)`（无 newCall 旧路径）。
- JDK-8 与 JDK-27 的同名类源码逐字节一致（svuid=2 双侧相同），JDK-27 仍保留客户端 stub（仅 IIOP 生成物被移除）。
来源：javap 双侧对照 + OpenJDK 源树 `javax/management/remote/rmi/`。

## 4. 测试用例设计
白盒：final/RemoteTest 层级/`(RemoteRef)` ctor/svuid=2/Serializable/无 getVersion；方法面（addNotificationListener(s)/close 等）。
黑盒深化：
- **RecordingRef**（实现 `java.rmi.server.RemoteRef` 记录 method/args）：注入 stub 后验证每个调用（`getConnectionId` 返回 reply `"rec-7"`、`getAttribute` 观察三参、`close`、`fetchNotifications`（reply 置 null，返回值被忽略不 cast）、`isRegistered`（reply=Boolean.FALSE 防拆箱 NPE）、`getMBeanCount`（reply=Integer.valueOf(0)））都走 `ref.invoke` —— 证明生成 stub 100% 委托 RemoteRef；`new RMIConnectionImpl_Stub(null).getConnectionId()` → NPE。
- **真线 stub**：`RMIJRMPServerImpl`+`RMIConnectorServer` 回环，`newClient(null)` 返回 `instanceof RMIConnectionImpl_Stub`，线级 `getAttribute`/`invoke add(6)` 真值、连接 id 前缀；`rc.close()` 后死 stub `getConnectionId` → IOException。

## 5. 深化（2026-11）与版本差异
- RecordingRef 委托证明为新深化（此前仅结构反射）；`@SuppressWarnings("deprecation")` 因实现 `RemoteRef` 的废弃方法（newCall/invoke(RemoteCall)/done）。
- 坑：stub 对返回值做生成期 cast——`fetchNotifications` 的 reply 必须置 null，否则 String→NotificationResult CCE。
- svuid=2 双侧一致；JDK-27 侧同名文件同结构（域名 jck.cstub27 区分）。
- 本类在两个版本均存在（非 IIOP 生成物），双侧测试一致。
