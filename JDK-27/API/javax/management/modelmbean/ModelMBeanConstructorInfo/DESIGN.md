# javax.management.modelmbean.ModelMBeanConstructorInfo — 设计案

## 1. API 功能
- 具体值类，继承 `MBeanConstructorInfo` 并实现 `DescriptorAccess`。
- 构造器：`(name,desc,MBeanParameterInfo[])`、带 Descriptor 的 `(name,desc,MBeanParameterInfo[],Descriptor)`、基于 `Constructor<?>` 的 `(description,Constructor<?>)`（其中名字取构造器的声明类名）。
- Descriptor 默认含 `name` 与 `descriptorType=operation`。
- 注意：拷贝构造 `ModelMBeanConstructorInfo(ModelMBeanConstructorInfo)` 为包私有，不属于 public API。

## 2. Java 用户如何使用
描述 ModelMBean 暴露的构造器元数据，供 MBeanInfo 与动态调用使用。
```java
ModelMBeanConstructorInfo c = new ModelMBeanConstructorInfo(
    "Ctor", "desc", new MBeanParameterInfo[0]);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/ModelMBeanConstructorInfoTest.java` 覆盖构造器与 clone/setDescriptor。
- 实现权威：`src/java.management/share/classes/javax/management/modelmbean/ModelMBeanConstructorInfo.java`。
- 说明：基于 `Constructor<?>` 的构造器将第一个字符串参数作为 description，name 由 `constructor.getName()`（声明类二进制名）推导。

## 4. 测试用例设计
- 白盒：继承 MBeanConstructorInfo、实现 DescriptorAccess、多个 public 构造器。
- 黑盒：基础构造器字段与签名往返；descriptor 默认 name/descriptorType；带 Descriptor 构造器保留 extra；Constructor 构造器推导 name=二进制名、description=入参、无参签名；clone 相等；setDescriptor 更新。
