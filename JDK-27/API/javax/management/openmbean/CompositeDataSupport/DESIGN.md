# javax.management.openmbean.CompositeDataSupport — 测试设计

## 1. API 功能
CompositeData 接口的唯一 JDK 实现：`CompositeDataSupport(CompositeType, String[], Object[])`（数组按 item 名排序、
逐项校验类型可赋值）与 `(CompositeType, Map)`（map 含全部且仅含合法 item 名）。查询 containsKey/containsValue/get/getAll/
getCompositeType/keySet；equals/hashCode/toString；values 不保证顺序故测试需排序。

## 2. Java 用户如何使用
用 CompositeType 声明结构，再以 itemNames/values 数组或 Map 构造支持实例，随后以 item 名 get 取值、
containsValue 判值、getAll 取全部，equals 比较两个等结构等值实例。
链路：CompositeType → CompositeDataSupport 构造 → get/containsValue/getAll → equals/hashCode/toString。

## 3. 开源代码实际用法调研
- JDK 自用：`sun.management.*CompositeData`（如 MemoryUsageCompositeData）以 CompositeDataSupport 构造
  并暴露为 CompositeData（`~/git/jdk25u-dev/src/java.management/share/classes/sun/management/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/CompositeDataSupportTest.java`、
  `NullConstructorParamsTest.java` 覆盖构造、null 参数与 equals。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（CompositeDataSupportTest.java）
- 白盒：public、implements CompositeData、getCompositeType/get/getAll/containsKey/containsValue 方法存在。
- 黑盒：数组/Map 两种构造；get 命中/缺失/null key；containsKey/containsValue（含 null 值）；getAll 键集与值；
  getCompositeType 身份；equals/hashCode（等值相等、异值/异型不等）；toString 含 className 与 item 名；
  异常精确 message（key 缺失 InvalidKeyException、item 名非法 InvalidOpenTypeException、类型不匹配
  InvalidOpenTypeException、缺少 item OpenDataException、null type/items/map NPE）。
