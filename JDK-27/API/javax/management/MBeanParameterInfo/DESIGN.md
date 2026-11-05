# javax.management.MBeanParameterInfo 黑盒行为测试设计

## 1. API 功能
- 构造：`(String name, String type, String description)` 及带 Descriptor 的重载。
- 查询：`getType()`、继承的 name/description/getDescriptor；`clone()`、`equals()`。

## 2. Java 用户如何使用
描述操作/构造器参数。
```java
MBeanParameterInfo p = new MBeanParameterInfo("count", "int", "the count");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/NullInfoArraysTest.java`。
- `test/jdk/javax/management/mxbean/*Parameter*Test.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanParameterInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getType 返回入参；clone 等值；equals 敏感；带 Descriptor 构造返回该值。
- 白盒：public 类、父类 MBeanFeatureInfo、implements Cloneable、含 getType。

## 5. 深化（批 B 追加）
- equals 参与矩阵：name、type、description、descriptor 全参与。
- hashCode 仅由 name+type 决定。
- null descriptor → 空 ImmutableDescriptor（无字段，非 null）。
- toString 格式含类名、name、type；序列化 round-trip。
- 版本差异：无。
