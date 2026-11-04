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
