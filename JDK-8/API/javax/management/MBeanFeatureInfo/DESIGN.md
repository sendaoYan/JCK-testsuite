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

## 5. 深化（批 B 追加）
- null 语义：name/description 为 null 时原样保留；equals 对 null 字段安全（null==null 判等）；hashCode 在 name 为 null 时抛 NPE。
- 子类不对称 equals：`new MBeanFeatureInfo(f).equals(new MBeanAttributeInfo(f))` == true，反向 == false（MBeanAttributeInfo.equals 要求对方是 MBeanAttributeInfo）——记录既有契约而非缺陷。
- 默认描述符为空 ImmutableDescriptor；null descriptor 同样得到空描述符。
- 序列化 round-trip：name/description/descriptor 保持。
- 版本差异：无。
