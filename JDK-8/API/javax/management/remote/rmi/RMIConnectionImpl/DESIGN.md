# RMIConnectionImpl 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnectionImpl` 是 `RMIConnection` 的服务端实现（public 类，可直构造实现本地路由）。

- 唯一 public 构造器：`(RMIServerImpl, String connectionId, ClassLoader, Subject, Map env)`；`server==null || connectionId==null` → NPE `"Illegal null argument"`；null env 容忍。
- 全部 `RMIConnection` 方法直通宿主 `RMIServerImpl` 的 MBeanServer（`MarshalledObject` 参数就地解包）。
- `close()` 幂等；`fetchNotifications(0,10,0)` 无监听时立即返回空结果。
- Subject delegation（`env` 中 `javax.management.remote.subject.delegation` 凭据）在本版本抛 `SecurityException("Subject delegation cannot be enabled unless an authenticated subject is put in place")`。

## 2. Java 用户如何使用
```java
RMIServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
RMIConnectorServer cs = new RMIConnectorServer(url, env, impl, mbs);
cs.start();  // newClient 内部构造 RMIConnectionImpl 并导出
```
直构造（不经连接器服务器）可用于进程内测试：绑定一个自定义 `RMIServerImpl`（覆写 `clientClosed` 跳过记账）即可本地路由。

## 3. 开源/源码实际用法调研
- JDK 源 `RMIConnectionImpl.java`（jdk8u-dev）：ctor null 检查、`mbeanServer = rmiServer.getMBeanServer()`（无 MBeanServer 时构造 NPE 于 `getClassLoaderRepository`）、`checkNonNull` 产生 `RuntimeOperationsException` 包装 IAE。
- 服务端错误按 JMX 远程规范包装为 `IOException` 子类（`AttributeNotFoundException`/`InstanceNotFoundException`/`MBeanException`/`ReflectionException`）后编组。
来源：JDK 仓库源码 + 双侧 jtreg 实证。

## 4. 测试用例设计
白盒：public 类、唯一 5 参构造器、非 final、非 Serializable。
黑盒深化（TinyServer 直构造）：
- ctor 负例：null server / null id → NPE `"Illegal null argument"`；null env 容忍。
- 本地路由：`getConnectionId` 往返、`toString` 含 `connectionId=`、`getAttribute`/`invoke`/`setAttribute`+回读、`getAttributes`、`isRegistered` true/false、`getMBeanCount==2`、`getDefaultDomain`、`getDomains`、`getMBeanInfo`、`isInstanceOf` 正反、`getObjectInstance`、`createMBean`、`queryNames`、`unregisterMBean`。
- 负例：`invoke(null name,…)` → `RuntimeOperationsException` 包 IAE `"ObjectName must not be null"`；`invoke(null op,…)` → `"Operation name must not be null"`；delegation subject → 精确 SecurityException 消息（见 §1）。
- quirk：`close()` 幂等且 close 后 `getConnectionId` 仍可读、本地路由仍可用（对象未失效，仅从服务器账户移除）；`unreferenced()` 连续两次无害。

## 5. 深化（2026-11）与版本差异
- 构造依赖 quirk：宿主 impl 必须已挂 MBeanServer，否则构造器在 `mbeanServer.getClassLoaderRepository()` 处 NPE（无自定义消息）。
- **版本差异（JDK-27）**：delegation subject 由 `SecurityException`（精确消息）变为 `UnsupportedOperationException("Subject Delegation has been removed.")`——JDK-27 移除了 Subject delegation 特性；其余消息双侧一致。
- 其余 27 方法行为双侧一致；JDK-27 侧见 `JDK-27/API/javax/management/remote/rmi/RMIConnectionImpl/`。
