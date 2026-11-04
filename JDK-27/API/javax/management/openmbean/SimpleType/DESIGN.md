# javax.management.openmbean.SimpleType — 测试设计

## 1. API 功能
预定义全部 open data 标量类型（无 public 构造器，私有构造 + 静态常量单例）：
14 个常量 VOID/BOOLEAN/CHARACTER/BYTE/SHORT/INTEGER/LONG/FLOAT/DOUBLE/STRING/
BIGDECIMAL/BIGINTEGER/DATE/OBJECTNAME；继承 OpenType 的 getClassName/getTypeName/getDescription/isArray，
覆盖 isValue/equals/hashCode/toString。

## 2. Java 用户如何使用
`SimpleType.STRING` 直接作为 OpenType 参数传给 CompositeType/TabularType/ArrayType；
`SimpleType.INTEGER.isValue(x)` 判定 x 是否为精确 Integer 类；equals 判定两个 SimpleType 是否同 className。
链路：选常量 → 组 CompositeType itemTypes → isValue 校验实例 → equals/hashCode 判同。

## 3. 开源代码实际用法调研
- JDK 自用：`sun.management.MemoryUsageCompositeData` 等 `sun.management.*CompositeData` 用 `SimpleType.LONG/INTEGER`
  作为 itemTypes 组装 CompositeType（`~/git/jdk25u-dev/src/java.management/share/classes/sun/management/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/IsValueTest.java`、`EqualsTest.java`
  覆盖 SimpleType/CompositeType/TabularType 的 isValue 与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用，未用 WebSearch）。

## 4. 测试用例设计（SimpleTypeTest.java）
- 白盒：final public、extends OpenType、单一 private 构造器、14 个 static final SimpleType 字段。
- 黑盒：14 常量逐个断言 className==typeName==description 且 !isArray；isValue 精确类匹配（Integer vs Long/String、
  null 拒绝、各包装/大数/Date/ObjectName/VOID）；equals（同/异 className、null、非 SimpleType）；
  hashCode == className.hashCode 且稳定；toString 精确格式 `javax.management.openmbean.SimpleType(name=java.lang.Integer)`。
