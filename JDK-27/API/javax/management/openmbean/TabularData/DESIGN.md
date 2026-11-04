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
