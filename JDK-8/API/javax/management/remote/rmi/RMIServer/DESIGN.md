# RMIServer 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIServer` 是 RMI 连接器的服务器级远程接口。

- 恰好 2 个方法：`String getVersion() throws RemoteException`（声明 RemoteException）；`RMIConnection newClient(Object credentials) throws IOException`（**不声明 RemoteException**——javap 双侧证实，仅声明 IOException）。
- 接口为远程接口（extends `java.rmi.Remote`）但异常声明不对称是本接口的核心契约点。

## 2. Java 用户如何使用
```java
RMIServer stub = (RMIServer) impl.toStub();
String version = stub.getVersion();     // "1.0 java_runtime_<version>"
RMIConnection rc = stub.newClient(null);
```
客户端经 `JMXConnectorFactory` 间接拿到（stub 序列化在 URL 中）。

## 3. 开源/源码实际用法调研
- JDK 源 `RMIServer.java`（jdk8u-dev）：方法集即上述 2 个；`RMIServerImpl` 抽象实现、`RMIJRMPServerImpl`/`RMIIIOPServerImpl`（8 only）具体化。
- 实现抛出的凭据异常原样编组给客户端（透明重抛）。
来源：javap 双侧 + JDK 源码。

## 4. 测试用例设计
白盒：恰好 2 方法、名称/参数/返回类型/异常声明矩阵（getVersion→RemoteException；newClient→IOException 且无 RemoteException）、接口 abstract。
黑盒深化：
- 手写实现（非 RMI 导出）：`getVersion` 返回哨兵、`newClient(null)` 返回哨兵连接——接口可本地实现。
- 凭据异常透明性：实现内 `throw new SecurityException(...)`（java.lang 包）原样抛给调用者，不包装。
- 真 stub 回环：`(RMIServer) impl.toStub()`、`getVersion` 等值（`"1.0 "` 前缀）、`newClient(null)` → 可用 `RMIConnection`（`getAttribute` 真值）；服务器 stop 后死 stub `newClient` → IOException。

## 5. 深化（2026-11）与版本差异
- 关键修正点：初版误断言 `newClient` 声明 RemoteException——javap 证实只有 `getVersion` 声明之；测试已按实际锁定。
- 双侧（JDK-8/JDK-27）方法集完全一致；JDK-27 侧域名区分（jck.rs27）。
