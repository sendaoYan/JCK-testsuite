# javax.management.MBeanFeatureInfo 黑盒行为测试设计

## 1. API 功能
- MBeanAttributeInfo/MBeanConstructorInfo/MBeanOperationInfo/MBeanNotificationInfo 的公共父类。
- 方法：`getName()`、`getDescription()`、`getDescriptor()`；`clone()`、`equals()`。

## 2. Java 用户如何使用
一般不直接实例化，作为元数据类公共字段的抽象。
```java
String name = feature.getName();
Descriptor d = feature.getDescriptor();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/*Test.java`：元数据 equals/hashCode。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanFeatureInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：通过子类验证 getName/getDescription 返回构造入参；默认 descriptor 为空；带 Descriptor 构造后 getDescriptor 返回该值；clone 等值。
- 白盒：public 抽象类、implements Cloneable、implements Serializable、含 getName/getDescription/getDescriptor。
