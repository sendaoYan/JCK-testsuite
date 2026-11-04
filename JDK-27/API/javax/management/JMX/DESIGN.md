# javax.management.JMX 黑盒行为测试设计

## 1. API 功能
- 常量：`DEFAULT_VALUE_FIELD`、`IMMUTABLE_INFO_FIELD`、`INTERFACE_CLASS_NAME_FIELD`、`LEGAL_VALUES_FIELD`、`MAX_VALUE_FIELD`、`MIN_VALUE_FIELD`、`MXBEAN_FIELD`、`OPEN_TYPE_FIELD`、`ORIGINAL_TYPE_FIELD`。
- 静态方法：`isMXBeanInterface(Class)`、`newMBeanProxy(MBeanServerConnection, ObjectName, Class)`、`newMXBeanProxy(MBeanServerConnection, ObjectName, Class, boolean)`、`getMBeanServerId(MBeanServerConnection)`。

## 2. Java 用户如何使用
创建 MBean/MXBean 的本地代理，或查询 MBeanServer 标识。
```java
ThingMBean proxy = JMX.newMBeanProxy(mbsc, on, ThingMBean.class);
String id = JMX.getMBeanServerId(mbsc);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/proxy/JMXProxyTest.java`：newMBeanProxy 代理调用。
- `test/jdk/javax/management/mxbean/MXBeanInteropTest1.java`：MXBean 判定。
- 实现权威：`src/java.management/share/classes/javax/management/JMX.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：isMXBeanInterface 对标准 MBean 接口为 false、对 MXBean 接口为 true；newMBeanProxy 生成代理并转发 getter；getMBeanServerId 返回非空、与 MBeanServerDelegate MBeanServerId 一致；描述符常量字符串非空。
- 白盒：public 具体类、静态方法、public 常量字段存在。
