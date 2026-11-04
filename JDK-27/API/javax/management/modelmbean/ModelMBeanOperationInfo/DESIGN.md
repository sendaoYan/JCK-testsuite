# javax.management.modelmbean.ModelMBeanOperationInfo — 设计案

## 1. API 功能
- 具体值类，继承 `MBeanOperationInfo` 并实现 `DescriptorAccess`。
- 构造器：`(name,desc,MBeanParameterInfo[],returnType,impact)`、带 Descriptor 的完整构造、基于 `Method` 的 `(description,Method)`、拷贝构造。
- Descriptor 默认含 `name`、`descriptorType=operation`、`role=operation`。

## 2. Java 用户如何使用
描述 ModelMBean 暴露的操作元数据，供动态 invoke。
```java
ModelMBeanOperationInfo op = new ModelMBeanOperationInfo(
    "concat", "desc", params, "java.lang.String", MBeanOperationInfo.ACTION);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/ModelMBeanOperationInfoTest.java` 覆盖构造器、clone、setDescriptor。
- 实现权威：`src/java.management/share/classes/javax/management/modelmbean/ModelMBeanOperationInfo.java`。
- 说明：setDescriptor 校验 name/descriptorType/role，不匹配会抛 RuntimeOperationsException。

## 4. 测试用例设计
- 白盒：继承 MBeanOperationInfo、实现 DescriptorAccess、多个 public 构造器。
- 黑盒：基础构造器 name/desc/returnType/impact/signature 往返；descriptor 默认字段；带 Descriptor 构造器保留 extra 与 impact；Method 构造器推导返回类型与签名；拷贝构造 equals；clone 相等；setDescriptor 更新。
