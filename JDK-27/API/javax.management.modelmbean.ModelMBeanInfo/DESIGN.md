# javax.management.modelmbean.ModelMBeanInfo — 设计案

## 1. API 功能
- 接口，描述 ModelMBean 的完整元数据，扩展普通 MBeanInfo 的描述符访问能力。
- 核心方法：`getDescriptors(String inDescriptorType)`、`getDescriptor(String name, String descriptorType)`、`getDescriptor(String name)`、`setDescriptor(Descriptor, String)`、`setMBeanDescriptor(Descriptor)`、`getMBeanDescriptor()` 及按类型取 attribute/operation/constructor/notification 的便捷方法。

## 2. Java 用户如何使用
通过实现类（`ModelMBeanInfoSupport`）构建 ModelMBean 的元数据图，再交给 `RequiredModelMBean`。
```java
ModelMBeanInfo info = new ModelMBeanInfoSupport(
    "C", "desc", attrs, ctors, ops, notifs);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/ModelMBeanInfoSupportTest.java` 覆盖描述符按类型查询/更新。
- 说明：接口把 JMX 元数据划分为 mbean/attribute/operation/constructor/notification 五类描述符，测试聚焦按名/按类型的检索。

## 4. 测试用例设计
- 白盒：接口、方法含 getDescriptors/getDescriptor/setMBeanDescriptor/getMBeanDescriptor/getAttribute/getOperation/getConstructor/getNotification。
- 黑盒：由 ModelMBeanInfoSupport 实现验证各按类型查询返回匹配描述符、`getDescriptor(name)` 单参数搜索、`getDescriptors(null)` 返回全部、setDescriptor 更新指定类型。
