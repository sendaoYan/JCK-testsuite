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

## 5. 深化（批 B 追加）
- null 四数组→空数组；空数组 getter 共享同一实例（`==` 判定），非空数组 getter 为防御性拷贝且每次调用返回新副本（两侧一致，均断言）。
- clone() 浅拷贝：副本与原对象共享元素实例。
- equals 参与矩阵：className、description、四个数组内容、descriptor 全部参与；任一不同即不等。
- null description 保留为 null（不默认空串）；默认 descriptor 为空 ImmutableDescriptor（无字段），显式 null 同样得到空描述符。
- 序列化 round-trip：equals 保持；null description 仍为 null。
- 版本差异：无（JDK-8 与 JDK-27 行为一致）。
