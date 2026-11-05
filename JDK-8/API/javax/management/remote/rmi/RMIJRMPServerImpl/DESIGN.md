# RMIJRMPServerImpl 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIJRMPServerImpl`（extends `RMIServerImpl`）是 JRMP 传输的连接器服务器实现。

- public 4 参构造器 `(int port, RMIClientSocketFactory, RMIServerSocketFactory, Map env)`；负端口 → IAE `"Negative port: -1"`。
- protected 钩子 `export/makeClient/closeClient/closeServer/getProtocol`（"rmi"）；非 Serializable；未挂 connector server 时可独立构造。
- 未导出行为：`toStub()`/`close()` → `NoSuchObjectException("object not exported")`；`newClient` 无 MBS → ISE `"Not attached to an MBean server"`。
- `getVersion()` = `"1.0 java_runtime_" + 版本`。

## 2. Java 用户如何使用
```java
RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null); // 0=任意可用端口
RMIConnectorServer cs = new RMIConnectorServer(
        new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, impl, mbs);
cs.start();   // impl.export() 导出 JRMP 对象
RMIServer stub = (RMIServer) impl.toStub();  // instanceof RMIServerImpl_Stub
```

## 3. 开源/源码实际用法调研
- JDK 源 `RMIJRMPServerImpl.java`（jdk8u-dev）：`export` 用 `UnicastRemoteObject.exportObject(this, port, csf, ssf)`；`makeClient` 构造 `RMIConnectionImpl` 并导出。
- `RMIConnectorServer` 工厂路径（未显式传 impl 时）内部构造本类。
来源：JDK 源码 + 双侧 jtreg 实证。

## 4. 测试用例设计
白盒：protected 钩子方法集、public 4 参 ctor、非 final、非 Serializable。
黑盒深化：
- 未导出：`getVersion` 前缀、`toStub` → NoSuchObjectException `"object not exported"`、`newClient` 无 MBS ISE、负端口 IAE 精确消息、`close` 未导出 `"object not exported"`。
- 一体化 live：4 参 ctor 挂 connector server → start → `toStub() instanceof RMIServerImpl_Stub`、`stub.getVersion()==impl.getVersion()`、`stub.newClient(...)` → `RMIConnectionImpl_Stub`、协议级 `getAttribute`/`invoke add(4)`/`queryNames`/`isRegistered`/`getDefaultDomain`/`getDomains`/`getMBeanInfo`/`isInstanceOf`、连接 id 记账与 `rc.close()` 移除；`cs.stop()` 后死 stub `newClient` → IOException（stub 引用在 stop 前捕获）。

## 5. 深化（2026-11）与版本差异
- 坑：`cs.stop()` 后 `impl.toStub()` 本身抛 NoSuchObjectException（对象已 unexport）——死 stub 负例必须先保存 stub 引用。
- 消息双侧一致；JDK-27 侧同名目录同结构（域名区分）；`port=0`（动态端口、仅回环）双侧可用。
