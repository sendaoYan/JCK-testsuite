# MBeanServerForwarder 测试设计

## 1. API 功能
`javax.management.remote.MBeanServerForwarder`（接口，继承 `MBeanServer`）在 `MBeanServer` 请求链中作中间转发器，可拦截/改写请求。

- 方法：`MBeanServer getMBeanServer()`、`void setMBeanServer(MBeanServer)`（外加继承的全部 `MBeanServer` 方法）。

## 2. Java 用户如何使用
```java
MBeanServerForwarder fwd = ...; // 自定义转发器
fwd.setMBeanServer(delegate);
MBeanServer next = fwd.getMBeanServer();
```
典型组合链路：`JMXConnectorServer.setMBeanServerForwarder(fwd)` 把转发器插入请求链；转发器把请求转给 `getMBeanServer()`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：`setMBeanServerForwarder` 保存转发器并把 `MBeanServer` 指向其 `getMBeanServer()`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/MBeanServerForwarder.java`：接口定义。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、继承 `MBeanServer`、`getMBeanServer/setMBeanServer` 存在。
黑盒轻行为：用 `java.lang.reflect.Proxy` 动态实现接口（避免手写约 30 个 `MBeanServer` 方法），验证 `setMBeanServer → getMBeanServer` 往返。
