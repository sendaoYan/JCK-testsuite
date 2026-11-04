# javax.management.openmbean.CompositeType — 测试设计

## 1. API 功能
描述 CompositeData 值结构：`CompositeType(typeName, description, itemNames[], itemDescriptions[], itemTypes[])`
构造（itemNames 内部排序、去空格、去重）。查询 containsKey/getDescription/getType/keySet/isValue；
覆盖 equals/hashCode/toString。className 固定为 `javax.management.openmbean.CompositeData`。

## 2. Java 用户如何使用
先声明 itemNames/itemDescriptions/itemTypes 三个数组构造 CompositeType，再用它构造 CompositeDataSupport。
链路：构造（校验数组等长/非空/去重）→ keySet 取 item 名 → getType 取 item 类型 → isValue 校验 CompositeData。

## 3. 开源代码实际用法调研
- JDK 自用：`sun.management.MemoryUsageCompositeData` 定义 `compositeType` 静态字段，用 itemNames/Descriptions/
  Types 构造 CompositeType（`~/git/jdk25u-dev/src/java.management/share/classes/sun/management/MemoryUsageCompositeData.java`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/ConstraintTest.java`、
  `NullConstructorParamsTest.java`、`EqualsTest.java` 覆盖构造校验、null 参数与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（CompositeTypeTest.java）
- 白盒：public、extends OpenType、containsKey/getDescription/getType/keySet 方法存在。
- 黑盒：className/typeName/description/!isArray；keySet 升序；containsKey/getType/getDescription 的命中/缺失/null；
  item 名去空格；isValue（同型真、null/非 CompositeData 假、子集/超集可赋值方向）；equals/hashCode；
  toString 前缀与 itemName；异常精确 message（重复 item 名 OpenDataException、空 item 名 IAE、数组不等长 IAE、
  null typeName IAE）。
