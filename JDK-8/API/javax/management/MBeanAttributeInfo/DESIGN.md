# javax.management.MBeanAttributeInfo 黑盒行为测试设计

## 1. API 功能
- 构造：`(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs)` 及带 Descriptor 的重载。
- 查询：`getType()`、`isReadable()`、`isWritable()`、`isIs()`、继承的 name/description/getDescriptor。

## 2. Java 用户如何使用
标准 MBean 内省生成 MBeanInfo 时，为每个属性生成一个 MBeanAttributeInfo。
```java
MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, true, false);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/NotificationInfoTest.java`、`NullInfoArraysTest.java`。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`：属性顺序。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanAttributeInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造后 getType/isReadable/isWritable/isIs 返回入参；getDescriptor 默认空 descriptor；equals 对关键字段敏感；clone 返回等值对象。
- 白盒：public 类、父类 MBeanFeatureInfo、implements Cloneable、含 getType/isReadable/isWritable/isIs。
