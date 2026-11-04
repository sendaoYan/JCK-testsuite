# javax.management.openmbean.ArrayType — 测试设计

## 1. API 功能
描述 open data 数组类型：`ArrayType(int, OpenType)`、`ArrayType(SimpleType, boolean)` 两个 public 构造器，
静态工厂 `getArrayType(OpenType)`（多维可链式）与 `getPrimitiveArrayType(Class)`（primitive 数组）。
查询 getDimension/getElementOpenType/isPrimitiveArray；覆盖 isValue/equals/hashCode/toString。

## 2. Java 用户如何使用
`ArrayType.getArrayType(SimpleType.STRING)` 得到 String[] 类型；`ArrayType.getPrimitiveArrayType(int[][][].class)`
得到 3 维 int 数组类型；作为 CompositeType 的 itemType 表达数组字段。
链路：工厂/构造 → getDimension/getElementOpenType → isValue 校验数组 → equals/hashCode 判同。

## 3. 开源代码实际用法调研
- JDK 自用：`com.sun.jmx.mbeanserver.DefaultMXBeanMappingFactory` 与 `OpenType` 映射用 ArrayType 表达数组映射
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/ArrayTypeTest.java`
  覆盖 primitive/non-primitive 数组 className、dimension、getElementOpenType。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（ArrayTypeTest.java）
- 白盒：public、extends OpenType、getArrayType/getPrimitiveArrayType 方法存在。
- 黑盒：String[]/int[]/2 维 Integer[][]/链式嵌套/3 维 primitive 的 className/typeName/description/dimension/
  isPrimitiveArray/isValue；equals/hashCode（独立实例相等、维度/元素/primitive 差异不等）；toString 前缀与关键字段；
  异常精确 message（dimension<1、非 primitive 元素、非数组 class、非 primitive component）。


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
