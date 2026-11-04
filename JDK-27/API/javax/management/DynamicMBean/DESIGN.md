# javax.management.DynamicMBean 黑盒行为测试设计

## 1. API 功能
- 接口：`getAttribute/setAttribute/getAttributes/setAttributes/invoke/getMBeanInfo`。

## 2. Java 用户如何使用
动态 MBean 在运行时通过 getMBeanInfo 提供元数据，并由 MBeanServer 调 get/set/invoke 完成操作。
```java
public class MyMBean implements DynamicMBean { ... }
mbs.registerMBean(new MyMBean(), on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/MBeanTest.java`：DynamicMBean 注册与调用。
- `test/jdk/javax/management/MBeanInfo/*Test.java`：元数据一致性。
- 实现权威：`src/java.management/share/classes/javax/management/DynamicMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用一个最小 DynamicMBean 注册到平台服务器，验证 getAttribute/setAttribute 往返、getAttributes/setAttributes 批量、invoke 调用、getMBeanInfo 返回元数据；finally 注销。
- 白盒：public 接口、含 getAttribute/setAttribute/invoke/getMBeanInfo。
