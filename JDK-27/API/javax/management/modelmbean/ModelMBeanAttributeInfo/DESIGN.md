# javax.management.modelmbean.ModelMBeanAttributeInfo — 设计案

## 1. API 功能
- 具体值类，继承 `MBeanAttributeInfo` 并实现 `DescriptorAccess`。
- 构造器：基础 `(name,type,desc,readable,writable,isIs)`、带 Descriptor 的 `(name,type,desc,readable,writable,isIs,Descriptor)`、基于 getter/setter Method 的构造、拷贝构造。
- Descriptor 默认含 `name`（属性名）与 `descriptorType=attribute`。

## 2. Java 用户如何使用
构建 ModelMBean 属性元数据，附额外描述符字段。
```java
ModelMBeanAttributeInfo a = new ModelMBeanAttributeInfo(
    "Name", "java.lang.String", "desc", true, true, false);
a.getDescriptor(); // name=Name, descriptorType=attribute
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/ModelMBeanAttributeInfoTest.java` 覆盖各构造器、clone、setDescriptor。
- 实现权威：`src/java.management/share/classes/javax/management/modelmbean/ModelMBeanAttributeInfo.java`。
- 说明：setDescriptor 会校验 descriptor 的 `name` 匹配属性名且 `descriptorType` 为 attribute，否则抛 RuntimeOperationsException。

## 4. 测试用例设计
- 白盒：继承 MBeanAttributeInfo、实现 DescriptorAccess、多个 public 构造器。
- 黑盒：基础构造器各字段往返；descriptor 默认 name/descriptorType；带 Descriptor 构造器保留 extra 字段；拷贝构造 equals；Method 构造器推导可读/可写与类型；clone 相等；setDescriptor 更新且保留匹配的 name。
