# javax.management.modelmbean.DescriptorSupport — 设计案

## 1. API 功能
- 具体类，实现 `javax.management.Descriptor`，是可变的键值描述符集合。
- 核心方法：`setField`/`getFieldValue`/`getFieldNames`/`getFieldValues`/`getFields`/`setFields`/`removeField`/`clone`/`equals`/`hashCode`/`isValid`。
- 字段名大小写不敏感；值可为任意 Object；多个构造器（无参、`Descriptor` 拷贝、`String[]/Object[]` 数组、`String...` 的 `name=value` 变长参数）。

## 2. Java 用户如何使用
用于构造 ModelMBean 元数据描述符，如 `name`、`descriptorType`、`displayName`、`persistPolicy`。
```java
Descriptor d = new DescriptorSupport("name=MyMBean", "descriptorType=mbean");
d.setField("displayName", "My Bean");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/DescriptorSupportTest.java` 系统覆盖 setField/getFields/equals/clone/isValid。
- 实现权威：`src/java.management/share/classes/javax/management/modelmbean/DescriptorSupport.java`。
- 说明：重点行为包括字段名大小写不敏感、String 值与包装值在 getFields 中的不同格式、`name=value` 变长构造。

## 4. 测试用例设计
- 白盒：非接口、实现 `Descriptor`、多个 public 构造器。
- 黑盒：setField/getFieldValue 的 String 与 Integer 往返、大小写不敏感查询、setFields 批量、getFieldValues 参数形式、getFields 的 `key=value` / `key=(value)` 格式、clone 独立与 equals/hashCode、removeField 及空值 no-op、isValid 需 name+descriptorType、异常路径（null/空字段名、数组长度不匹配）。
