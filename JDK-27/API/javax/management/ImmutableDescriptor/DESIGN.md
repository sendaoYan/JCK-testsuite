# javax.management.ImmutableDescriptor 黑盒行为测试设计

## 1. API 功能
- 不可变 Descriptor 实现；`EMPTY_DESCRIPTOR` 常量。
- 构造：String[]/Object[] 成对、`String...`（"name=value"）、`Map`。
- 方法：`getFieldValue/getFieldValues/getFieldNames`、`union(Descriptor...)`、`isValid`、`clone`、`equals/hashCode`。

## 2. Java 用户如何使用
创建不可修改的元数据描述符；字段名大小写不敏感；修改操作抛异常。
```java
Descriptor d = new ImmutableDescriptor("units=bytes", "description=size");
Descriptor merged = ImmutableDescriptor.union(d1, d2);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/descriptor/ImmutableDescriptorSetFieldsTest.java`：setField 抛异常。
- `test/jdk/javax/management/descriptor/UnionTest.java`：union 冲突抛 IllegalArgumentException。
- 实现权威：`src/java.management/share/classes/javax/management/ImmutableDescriptor.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：字段读取（大小写不敏感、absent null）；equals/hashCode 对字段名大小写不敏感、对值敏感；union 合并无冲突字段、对冲突值抛 IllegalArgumentException；varargs "name=value"、Map 构造；setField/setFields/removeField 抛 RuntimeOperationsException(UnsupportedOperationException)；clone() 返回同一实例（源码确认）。
- 白盒：public 类、implements Descriptor、EMPTY_DESCRIPTOR 存在。

## 5. 深化（批 B 追加）
- 构造异常矩阵（IAE，消息精确）："Different size arrays"、"Empty or null field name"、"Duplicate field name"（大小写不敏感）、"Missing = character: ..."、"Null Map" 等。
- 字段按大小写不敏感字母序存储：getFields/getFieldNames/toString/getFieldValues(null) 顺序一致。
- 变异接口 → ROE(UOE "Descriptor is read-only")；同值 setField / 缺席 removeField 为 no-op。
- union：单非空不可变输入恒等、空数组→EMPTY_DESCRIPTOR、null 元素跳过、冲突 IAE、null 数组 NPE、可变输入复制；isValid 恒 true；clone==self。
- 序列化 round-trip。【版本差异】JDK-27 反序列化后 hashCode 一致（readResolve 置 -1 强制重算）；JDK-8 侧为过期 0。
