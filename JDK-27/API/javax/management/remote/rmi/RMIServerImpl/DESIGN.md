# RMIServerImpl 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIServerImpl`（抽象类，实现 `Closeable`、`RMIServer`）是 RMI 连接器服务器端的公共基类。

- 构造器：`(Map<String,?> env)`。
- 方法：`getVersion()`（`"1.0 java_runtime_" + runtime.version`）、`newClient(Object)`、`close()`、`setMBeanServer/getMBeanServer`、`setDefaultClassLoader/getDefaultClassLoader`；受保护抽象钩子 `export/toStub/makeClient/closeClient/getProtocol/closeServer`。

## 2. Java 用户如何使用
由具体子类（`RMIJRMPServerImpl`）实现传输层；用户经 `RMIConnectorServer` 间接使用。可对子类调用 `setMBeanServer`/`getMBeanServer` 配置本地 MBean 服务器。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIServerImpl.java`：`getVersion()` 拼接 `System.getProperty("java.runtime.version")`；`getMBeanServer()` 返回 `mbeanServer` 字段（初值为 null）。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIJRMPServerImpl.java`：`extends RMIServerImpl`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：抽象类、实现 `Closeable`/`RMIServer`、公共方法与受保护钩子存在。
黑盒轻行为：匿名子类实现 6 个抽象钩子，验证 `getVersion` 前缀 `"1.0 "`、`set/getMBeanServer` 与 `set/getDefaultClassLoader` 往返、初值 null、`close()` 安全（`closeServer` 空实现）。

## 5. 深化（2026-11）
白盒新增：protected 钩子全集（含 `clientClosed/connectionOpened/connectionClosed`）、public `(Map)` ctor（null 容忍）、非 Serializable。
黑盒新增（TinyServer 最小子类，覆写 `clientClosed` 跳过 connServer 记账）：
- `setMBeanServer(null)`/`setDefaultClassLoader(null)` 容忍（get 返回 null）。
- `newClient` 无 MBS → ISE `"Not attached to an MBean server"`；挂 MBS 的裸 impl `newClient` → NPE（connServer 空引用，无自定义消息；检查顺序 ISE 优先）。
- `close()` 连续两次幂等。
版本差异：与 JDK-8 行为一致（基类未受 IIOP/Subject-delegation 移除影响）。
