# javax.management.openmbean.CompositeData — 测试设计

## 1. API 功能
open data 复合值的只读接口：containsKey/containsValue/get/getAll/getCompositeType/keySet/values，以及
重写 equals/hashCode/toString。核心实现为 CompositeDataSupport（见其测试）。

## 2. Java 用户如何使用
通常由 CompositeDataSupport 实现；用户以接口类型接收，用 get(itemName) 读字段、containsKey 判存在、
getCompositeType 取结构，遍历 keySet。链路：CompositeDataSupport → CompositeData 接口方法 → get/containsKey。

## 3. 开源代码实际用法调研
- JDK 自用：`sun.management.*CompositeData` 与 MXBean 映射统一以 CompositeData 接口暴露复合值
  （`~/git/jdk25u-dev/src/java.management/share/classes/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/CompositeDataTest.java` 覆盖接口契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（CompositeDataTest.java）
- 白盒：public interface、方法签名存在（containsKey/containsValue/get/getAll/getCompositeType/keySet/values）。
- 黑盒：匿名实现各方法，验证 containsKey/containsValue/get/getAll/keySet/values/getCompositeType 行为
  （轻行为，无 equals/hashCode 强约束）。


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
