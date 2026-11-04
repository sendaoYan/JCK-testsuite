# javax.management.openmbean.CompositeDataView — 测试设计

## 1. API 功能
标记接口，指示实现类提供 CompositeData 视图：`CompositeData toCompositeData(CompositeType ct)`。
含类型转换帮助。实现类可把自身转成指定 CompositeType 的 CompositeData。

## 2. Java 用户如何使用
自定义类实现 CompositeDataView，暴露 toCompositeData(CompositeType)，MXBean 映射时据此转 open data。
链路：自定义类 implements CompositeDataView → toCompositeData(ct) → CompositeDataSupport。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射检查类型是否 implements CompositeDataView 以提供自定义 open data 视图
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/CompositeDataViewTest.java` 覆盖契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（CompositeDataViewTest.java）
- 白盒：public interface、单一抽象方法 toCompositeData(CompositeType) 存在。
- 黑盒：匿名/具名实现 toCompositeData 返回 CompositeDataSupport；断言返回结构与字段（轻行为）。


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
