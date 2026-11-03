# javax.management.Descriptor 黑盒行为测试设计

## 1. API 功能
- 接口，描述 MBean 元数据字段。
- 方法：`getFieldValue/getFieldValues/getFieldNames`、`setField/setFields/removeField`、`isValid`、`clone`、`equals/hashCode`。

## 2. Java 用户如何使用
MBeanInfo/属性/操作等元数据都携带 Descriptor，用户读取 `immutableInfo`、`defaultValue`、`legalValues` 等字段；字段名大小写不敏感。
```java
Object v = descriptor.getFieldValue("description");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/descriptor/DescriptorTest.java`：字段读写。
- `test/jdk/javax/management/descriptor/EqualsHashCodeTest.java`：相等性契约。
- 实现权威：`src/java.management/share/classes/javax/management/Descriptor.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：ImmutableDescriptor 作实现验证 getFieldValue（大小写不敏感）、absent 返回 null、getFieldNames/getFieldValues、equals/hashCode 对字段值敏感、clone、setField/setFields/removeField 抛 RuntimeOperationsException(UnsupportedOperationException)。
- 白盒：public 接口、含 getFieldValue/clone/equals。
