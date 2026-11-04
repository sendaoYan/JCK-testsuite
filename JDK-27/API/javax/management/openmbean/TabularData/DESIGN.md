# javax.management.openmbean.TabularData — 测试设计

## 1. API 功能
open data 表格值的只读接口：按索引行做 key 的 Map 语义视图，方法 get/remove/containsKey/containsValue/put/
values/keySet/clear/size/calculateIndex/getTabularType，及 equals/hashCode/toString。核心实现为 TabularDataSupport。

## 2. Java 用户如何使用
通常由 TabularDataSupport 实现；用户以接口类型接收，用 get/remove/containsKey 按索引行操作，
values 遍历所有行，getTabularType 取结构。链路：TabularDataSupport → TabularData 接口方法 → get/values。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射统一以 TabularData 接口暴露表格类型
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/TabularDataTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（TabularDataTest.java）
- 白盒：public interface、方法签名存在（get/remove/containsKey/containsValue/put/values/keySet/clear/size/
  calculateIndex/getTabularType）。
- 黑盒：以 TabularDataSupport 实例经接口调用，验证 get/containsKey/values/size/clear/getTabularType 行为（轻行为）。


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
