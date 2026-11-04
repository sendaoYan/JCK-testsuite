# javax.management.MBeanInfo 黑盒行为测试设计

## 1. API 功能
- 构造：`(String className, String description, MBeanAttributeInfo[], MBeanConstructorInfo[], MBeanOperationInfo[], MBeanNotificationInfo[], Descriptor)`。
- 查询：`getClassName()`、`getDescription()`、`getAttributes()`、`getConstructors()`、`getOperations()`、`getNotifications()`、`getDescriptor()`；`clone()`、`equals()`。

## 2. Java 用户如何使用
DynamicMBean.getMBeanInfo 返回的完整元数据，管理工具据此渲染属性和操作。
```java
MBeanInfo info = mbs.getMBeanInfo(on);
MBeanAttributeInfo[] attrs = info.getAttributes();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/MBeanInfoEqualsTest.java`、`MBeanInfoEqualsNPETest.java`。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造后各 getter 返回入参；空数组构造合法；clone 等值；equals 对 className/数组内容敏感；带 Descriptor 返回该值。
- 白盒：public 类、implements Cloneable、implements Serializable、含 getClassName/getAttributes。
