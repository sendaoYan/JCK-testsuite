# javax.management.MBeanServer 黑盒行为测试设计

## 1. API 功能
- 接口（MBeanServerConnection 的子接口）：注册/注销 MBean，读/写属性，调用操作，查询 MBean，创建/查询 MBean 与类加载器。

## 2. Java 用户如何使用
所有 JMX 操作的核心入口，由 MBeanServerFactory 或平台服务器提供实例。
```java
MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
ObjectInstance oi = mbs.registerMBean(bean, on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/*Test.java`（注册/注销/异常/通知）。
- `test/jdk/javax/management/MBeanServerFactory/ReleaseMBeanServerTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServer.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用平台服务器注册 StandardMBean，验证 isRegistered/registerMBean/getAttribute/setAttribute/invoke/isInstanceOf/unregisterMBean；isInstanceOf 用实现类名（源码确认检查类而非接口）。
- 白盒：public 接口、父接口 MBeanServerConnection、含 registerMBean/unregisterMBean/queryNames 等。
