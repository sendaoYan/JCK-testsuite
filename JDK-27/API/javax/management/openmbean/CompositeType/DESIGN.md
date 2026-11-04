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


---

## 深化（2026-11，批次 F：openmbean 类型系统）

本轮追加维度与两侧实测结论（详细负例精确异常表见上文第 4 节，本轮已全部锁定为精确消息断言）：
- 序列化 round-trip 全覆盖（SimpleType 单例 readResolve ==、ArrayType/CompositeType/TabularType/
  CompositeDataSupport/TabularDataSupport equals 复活、null 字段值保留）。
- 不可变集合行为（CompositeType.keySet / TabularType.getIndexNames / CompositeDataSupport.values /
  TabularDataSupport.keySet 的 add → UnsupportedOperationException）。
- 构造路径等价与规范细节（ArrayType(SimpleType,boolean) 两分支、嵌套 primitive 维度相加、
  TabularType 重复索引名允许、CompositeType 0 字段不允许与 trim、描述不参与 CompositeType.equals、
  CompositeDataSupport.equals 要求同实现类）。
- CompositeDataView 经本地 MBeanServer 的 MXBean 真闭环；CompositeDataInvocationHandler 的
  Object 方法路由与全部精确消息负例。
- TabularDataSupport Map 视图语义（键为索引 List、Map.put 返回所放行 5090566、
  containsKey 吞 CCE vs get/remove 抛 CCE、无 3 参便捷 put、null type 构造期拒绝）。
- javap 双侧对照：11 类公开 API 面完全一致；异常消息逐字一致（除 JDK-27 NPE/CCE helpful 文本、
  JDK-9+ InaccessibleObjectException 两处，均按类型/双路径断言并记录）。
