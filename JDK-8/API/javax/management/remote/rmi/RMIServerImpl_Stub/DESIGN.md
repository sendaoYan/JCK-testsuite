# RMIServerImpl_Stub 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIServerImpl_Stub` 是 rmic 为 `RMIServerImpl` 生成的 JRMP 客户端 stub（服务器级）。

- final 类，extends `java.rmi.server.RemoteStub`，implements `RMIServer`；public `(RemoteRef)` 构造器；`serialVersionUID == 2`；Serializable。
- 仅暴露服务器级方法 `getVersion`/`newClient`（无 `getAttribute` 等连接级方法——与 `RMIConnectionImpl_Stub` 的区分点）。
- 每个远程调用经 `ref.invoke(...)` 编组；真实对象由 `RMIServerImpl.toStub()` 产生（`UnicastRemoteObject.exportObject` 的 stub）。

## 2. Java 用户如何使用
```java
RMIServerImpl_Stub stub = (RMIServerImpl_Stub) impl.toStub();
String v = stub.getVersion();            // 1.0 java_runtime_...
RMIConnection rc = stub.newClient(null); // 交给 RMIConnector
```
stub 序列化后 base64 编码进 JMXServiceURL（stub-encoded URL）即可跨进程分发。

## 3. 开源/源码实际用法调研
- rmic `-v1.2` 生成物；JDK-8 与 JDK-27 同源（svuid=2 双侧相同）。
- JDK-27 保留客户端 JRMP stub（仅移除 IIOP 生成物 `_Tie`/`_Stub`）。
来源：javap 双侧对照。

## 4. 测试用例设计
白盒：final/RemoteStub 层级/`(RemoteRef)` ctor/svuid=2/Serializable/无 getAttribute；方法面（getVersion/newClient）。
黑盒深化：
- **RecordingRef** 委托证明：`getVersion` 返回 reply `"1.0 recording"` 且 ref 观察 `getVersion`；`newClient("creds")` 观察凭据参数透传（reply=null 时返回 null）；`new RMIServerImpl_Stub(null).getVersion()` → NPE。
- **真线 stub**：`RMIJRMPServerImpl`+`RMIConnectorServer` 回环，`impl.toStub() instanceof RMIServerImpl_Stub`、真 `getVersion` `"1.0 "` 前缀、`newClient` → `RMIConnectionImpl_Stub`、线级 `getAttribute` 真值；`cs.stop()` 后死 stub `newClient` → IOException。

## 5. 深化（2026-11）与版本差异
- RecordingRef 模式新深化；凭据参数透传断言（`"creds"` 精确出现在 ref 记录的 args[0]）。
- svuid=2 双侧一致；JDK-27 侧同结构（域名 jck.sstub27，视 RMIServer 调用走接口视图）。
