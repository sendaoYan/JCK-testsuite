# javax.management.openmbean.TabularType — 测试设计

## 1. API 功能
描述 TabularData 值结构：`TabularType(typeName, description, rowType, indexNames[])` 构造（校验 rowType 非 null、
indexNames 非空且每个都是 rowType 的合法 item 名）。查询 getRowType/getIndexNames/isValue；覆盖 equals/hashCode/toString。
className 固定为 `javax.management.openmbean.TabularData`。

## 2. Java 用户如何使用
用 CompositeType 作 rowType、指定若干 item 名作索引列构造 TabularType，再构造 TabularDataSupport。
链路：构造 → getRowType/getIndexNames → TabularDataSupport(tabularType) → put 行（按索引列算 index）→ isValue。

## 3. 开源代码实际用法调研
- JDK 自用：`com.sun.jmx.mbeanserver` MXBean 映射用 TabularType 表达 `Map`/表格类型
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/TabularDataOrderTest.java`
  与 `ConstraintTest.java` 覆盖 TabularType 构造与 index 顺序。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（TabularTypeTest.java）
- 白盒：public、extends OpenType、getRowType/getIndexNames 方法存在。
- 黑盒：className/typeName/description/!isArray；rowType 身份与 equals；indexNames 保序不可变；isValue；
  equals/hashCode（独立实例、不同 typeName/indexNames/rowType 不等）；toString 前缀；
  异常精确 message（非法 index 名 OpenDataException、null rowType IAE、空 indexNames IAE、空 index 名 IAE）。


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
