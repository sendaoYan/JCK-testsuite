# RMIConnectorServer 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnectorServer`（extends `JMXConnectorServer`）是 RMI 连接器服务器。

- 常量（JDK-8）：`JNDI_REBIND_ATTRIBUTE="jmx.remote.jndi.rebind"`、`RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE="jmx.remote.rmi.client.socket.factory"`、`RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE="jmx.remote.rmi.server.socket.factory"`（本版本**无** `CREDENTIALS_FILTER_PATTERN`/`SERIAL_FILTER_PATTERN`——那是 JDK 后续版本新增，测试以 `NoSuchFieldException` 反射断言锁定）。
- 构造器负例：null URL → IAE `"Null JMXServiceURL"`；bogus 协议 → `MalformedURLException("Invalid protocol type: bogus")`；坏路径 → `MalformedURLException("URL path must be empty or start with /jndi/")`。
- 状态机：`start` 未挂 MBeanServer → ISE `"This connector server is not attached to an MBean server"`；未激活 `toJMXConnector` → ISE `"Connector is not active"`；stop-before-start 后 start → IOException `"The server has been stopped."`（永久失效）；start 幂等；stop 幂等；未激活 `getAddress()` null、`getConnectionIds()` 空。
- `setMBeanServerForwarder` 形成链：`getMBeanServer()` 返回最前端 forwarder。
- 注册为 MBean 后：`Active`(Boolean) 路由 `isActive`、`Address` 路由 `getAddress()`（协变返回 JMXServiceURL，属性读取需 toString 比较）、`ConnectionIds`(String[])、`start`/`stop` 操作路由。
- 服务端通知 OPENED/CLOSED（source==服务器）。
- iiop：本版本 URL `service:jmx:rmi+iiop://...` 可构造，但 start 会拉起非守护 CORBA 线程（见 §5），测试只构造不 start。

## 2. Java 用户如何使用
```java
RMIConnectorServer cs = new RMIConnectorServer(
        new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, mbs);
cs.start();                                  // stub base64 编码进 getAddress().getURLPath()
JMXServiceURL addr = cs.getAddress();        // service:jmx:rmi:///stub/rO0AB...
cs.stop();
```

## 3. 开源/源码实际用法调研
- JDK 源 `RMIConnectorServer.java`（jdk8u-dev）：`toJMXConnector` = `new RMIConnector(stub, usemap)`；`start` 校验顺序（stopped→attached→already-started）与上述状态机一致。
- stub-encoded URL 形态由 `RMIJRMPServerImpl.export` + `RMIConnector.encodeStub` 实现。
来源：JDK 源码 + 双侧 jtreg 实证。

## 4. 测试用例设计
白盒：继承 `JMXConnectorServer`、常量精确值、构造器/方法面。
黑盒深化：
- ctor 负例矩阵（§1 三条精确消息 + filter 常量缺失反射断言）。
- 生命周期：未激活状态四断言、start-无-MBS ISE、toJMXConnector 未激活 ISE、stop-before-start→start IOException 精确消息。
- Live：Proxy 录制型 `MBeanServerForwarder` 链（`cs.getMBeanServer()==fwd`、`fwd.getMBeanServer()==原`、远程调用经 forwarder）；start 幂等；地址 `/stub/` 前缀；连接 id 记账/关闭移除；`toJMXConnector` 激活态可用且为 RMIConnector；4 个服务端通知序列 opened/closed/opened/closed + source==cs；stop 幂等+addr null+start-after-stop IOException；注册 MBean 后 preRegister 自动挂载 + Active/Address(toString)/ConnectionIds + start/stop 操作路由。

## 5. 深化（2026-11）与版本差异
- **致命坑（JDK-8）**：`rmi+iiop` 连接器服务器 `start()` 留非守护 CORBA 线程，JVM 挂死（jtreg EXIT=124）——测试只构造不 start iiop。
- **版本差异（JDK-27）**：`rmi+iiop` 在构造期即被拒绝（`MalformedURLException "Invalid protocol type: iiop"`），IIOP 传输整体移除；且新增 `CREDENTIALS_FILTER_PATTERN`/`SERIAL_FILTER_PATTERN` 两常量（JDK-8 无）。
- `Address` 属性协变返回类型 quirk：接口声明 String 但实现返回 JMXServiceURL——`getAttribute` 读取后必须 toString 比较，不能 cast String。
- 其余消息双侧一致；JDK-27 侧见对应目录。
