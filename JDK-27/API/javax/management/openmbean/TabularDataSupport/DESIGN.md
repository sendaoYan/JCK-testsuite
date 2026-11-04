# javax.management.openmbean.TabularDataSupport — 测试设计

## 1. API 功能
TabularData 接口的唯一 JDK 实现：`TabularDataSupport(TabularType, int initialCapacity, float loadFactor)`
与 `(TabularType)` 构造。put（按索引列计算 index，重复 index 抛 KeyAlreadyExistsException）、get/remove/containsKey/
values/keySet/clear/size/calculateIndex/equals/hashCode；内部以 Map<CompositeData,CompositeData> 存储，value 为行本身。

## 2. Java 用户如何使用
用 TabularType 声明行结构与索引列，new TabularDataSupport 后以 CompositeData 为行 put；按索引列值构造
CompositeData 作 key 来 get/remove/containsKey；values 遍历所有行（迭代顺序不定需排序）。
链路：TabularType → TabularDataSupport → put 行 → get/remove/containsKey(values) → equals/hashCode。

## 3. 开源代码实际用法调研
- JDK 自用：`com.sun.jmx.mbeanserver` MXBean 映射用 TabularDataSupport 承载 Map 类型
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/TabularDataOrderTest.java`、
  `TabularDataSupportTest.java` 覆盖 put/index 顺序与 clear/size。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（TabularDataSupportTest.java）
- 白盒：public、implements TabularData、put/get/remove/containsKey/values/keySet/clear/size/calculateIndex 方法存在。
- 黑盒：两种构造器；put 成功与重复索引 KeyAlreadyExistsException 精确 message；get/remove/containsKey 命中/缺失；
  values/keySet 内容（排序后断言）；clear/size；calculateIndex 与 put 的 index 一致；equals/hashCode
  （相同行集相等、行数不同不等）；非法行类型 ClassCastException/异常路径。
