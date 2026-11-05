# javax.management.ImmutableDescriptor 黑盒行为测试设计

## 1. API 功能
- 不可变 Descriptor：字段名大小写不敏感、按大小写不敏感字母序存储；`EMPTY_DESCRIPTOR` 单例；`union(Descriptor...)` 合并。

## 2. Java 用户如何使用
作为 MBeanInfo 族元数据的只读描述符；@DescriptorKey 注解生成的字段即以它承载。
```java
Descriptor d = ImmutableDescriptor.union(a, b);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/descriptor/ImmutableDescriptorTest.java`、`UnionTest.java`、`DescriptorSupportTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/ImmutableDescriptor.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造/读取/不可变性/合并/序列化全契约。
- 白盒：类 public final、EMPTY_DESCRIPTOR 常量。

## 5. 深化（批 B 追加）
- 构造异常矩阵（IAE 而非 ROE，消息精确锁定）："Different size arrays"、"Empty or null field name"、"Duplicate field name: a"（A/a 视为重复）、"Null fields parameter"、"Null array parameter"、"Null Map"、"Missing = character: noequals"。
- "a=b=c" 按第一个 '=' 拆分；三个构造器形状等价。
- getFields/getFieldNames/toString 按大小写不敏感字母序（i,n,s）；getFieldValues(null) 返回全部字段值（同序）；toString 形如 "{i=5, n=null, s=str}"。
- 不可变矩阵：setField/removeField/setFields → ROE(UOE "Descriptor is read-only")；同值 setField 与缺席 removeField 为 no-op（不抛）；clone() == self（两种调用形式）。
- union 语义：单个非空不可变输入 → 恒等返回；空数组 → EMPTY_DESCRIPTOR；null 元素跳过；字段值冲突 → IAE（含大小写不敏感冲突）；整个数组为 null → NPE；可变输入被复制。
- isValid() 恒 true（'.' 字段名、任意值均 true）。
- EMPTY_DESCRIPTOR：toString "{}"、clone==self、序列化经 readResolve 恒等。
- 数组字段值被克隆存储：修改源数组不影响内部值。
- 序列化 round-trip equals 保持。【版本差异】JDK-8 反序列化后 hashCode 为过期的 0（transient 缓存未重算），JDK-27 readResolve 置 hashCode=-1 强制重算——两侧分别断言。
