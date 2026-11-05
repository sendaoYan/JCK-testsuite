# javax.management.DescriptorRead 黑盒行为测试设计

## 1. API 功能
- 接口：仅 `getDescriptor()`（@since 1.6）。MBeanInfo 族元数据的只读描述符视图。

## 2. Java 用户如何使用
元数据对象统一暴露描述符；管理工具读属性级元数据。
```java
Descriptor d = mbeanInfo.getAttributes()[0].getDescriptor();
```

## 3. 开源代码实际用法调研
- 实现权威：`src/java.management/share/classes/javax/management/DescriptorRead.java` 及 MBeanFeatureInfo 实现。

## 4. 测试用例设计 + 自动化实现
- 黑盒：接口结构 + 元数据族实现者闭环。
- 白盒：单方法接口、不继承 Serializable。

## 5. 深化（批 B 追加）
- 恰声明一个方法（getDescriptor）；不 extends Serializable。
- 元数据族（MBeanInfo/MBeanAttributeInfo/MBeanOperationInfo/MBeanParameterInfo/MBeanConstructorInfo/MBeanNotificationInfo + 基类 MBeanFeatureInfo）全部 instanceof DescriptorRead 且描述符永不为 null（默认空 ImmutableDescriptor）。
- MBeanInfo 构造提供的 ImmutableDescriptor("k=v") 原样保留。
- 匿名实现按恒等返回。
- 版本差异：无。
