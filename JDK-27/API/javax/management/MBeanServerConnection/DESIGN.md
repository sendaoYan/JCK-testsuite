# javax.management.MBeanServerConnection 黑盒行为测试设计

## 1. API 功能
- 接口：`getAttribute/setAttribute/getAttributes/setAttributes`、`invoke`、`queryMBeans/queryNames`、`getMBeanInfo/getObjectInstance/isRegistered/isInstanceOf/getMBeanCount/getDefaultDomain` 等。

## 2. Java 用户如何使用
连接本地或远程 MBeanServer 的抽象（MBeanServer 继承它）；JMX 代理也基于它。
```java
Object v = mbsc.getAttribute(on, "Count");
Set<ObjectName> names = mbsc.queryNames(new ObjectName("d:*"), null);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/proxy/JMXProxyTest.java`：基于连接创建代理。
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerConnection.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用平台服务器（MBeanServer 实现该接口）验证 getAttribute/setAttribute/getAttributes/invoke/getMBeanInfo/queryNames/isInstanceOf/getMBeanCount 的行为。
- 白盒：public 接口、含 getAttribute/invoke/queryMBeans/getMBeanInfo 等方法。
