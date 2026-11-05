# javax.management.Descriptor 黑盒行为测试设计

## 1. API 功能
- 接口：`getFieldValue/setField/getFields/getFieldNames/getFieldValues/removeField/setFields/clone/isValid`，extends Serializable、Cloneable。
- 两个标准实现：`ImmutableDescriptor`（只读）、`DescriptorSupport`（可变，位于 modelmbean 包）。

## 2. Java 用户如何使用
MBean 元数据（MBeanInfo 族）携带的键值对；注解 @DescriptorKey 生成的字段最终落到 Descriptor。
```java
Descriptor d = info.getDescriptor();
Object units = d.getFieldValue("units");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/descriptor/DescriptorTest.java`（接口契约）。
- 实现权威：`src/java.management/share/classes/javax/management/Descriptor.java`、`.../modelmbean/DescriptorSupport.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：接口结构（Serializable/Cloneable/方法全集）；ImmutableDescriptor 只读侧与 DescriptorSupport 可变侧。
- 白盒：接口声明的方法名逐一存在。

## 5. 深化（批 B 追加）
- 只读侧（ImmutableDescriptor 经接口使用）：字段名大小写不敏感（NAME==name）；setField/removeField → RuntimeOperationsException。
- 可变侧（DescriptorSupport）：setField/setFields/removeField round-trip；getFieldNames 按大小写不敏感字母序排序；getFields 非字符串值加括号（`a=(1)`）；clone 独立（改副本不影响原）；setField(null) → ROE。
- 【坑】`new DescriptorSupport("a=1")` 单 String 实参被解析为 XML 构造器（XMLParseException），必须显式传 `new String[]{"a=1"}` 走 varargs k=v 构造器。
- 已知字段值即时校验（构造/设置时即抛，而非 isValid 时）：visibility 1..4、severity 0..6、log 布尔、persistPolicy 枚举、数值字段 >=-1、name/descriptorType 必须 String → ROE(IAE "Field value invalid: ...")。
- isValid 严格：必须同时有非空 name 与 descriptorType；未知字段（含 '.' 名或任意对象值）不做值校验。
- 跨实现 equals/hashCode：ImmutableDescriptor 与内容相同的 DescriptorSupport 双向 equals 且 hashCode 一致（Util.hashCode 公式）。
- 版本差异：无。
