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
