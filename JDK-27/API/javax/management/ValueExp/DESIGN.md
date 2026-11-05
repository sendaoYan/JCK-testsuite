# javax.management.ValueExp 黑盒行为测试设计

## 1. API 功能
- 接口：`apply(ObjectName)`、`setMBeanServer(MBeanServer)`。

## 2. Java 用户如何使用
查询值表达式的标记接口，Query.attr/value 组合产生 ValueExp。
```java
ValueExp v = Query.plus(Query.attr("a"), Query.value(1));
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/SupportedQueryTypesTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/ValueExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：Query.value(String/Number/Boolean) 返回 ValueExp 实例；StringValueExp/AttributeValueExp 实现 apply。
- 白盒：public 接口、extends Serializable、含 apply/setMBeanServer。

## 深化（2026-11，批次 e）
新增维度：接口契约、**自定义 ValueExp 组合进标准算子**、序列化 round-trip。
- 接口：apply(ObjectName)→ValueExp、setMBeanServer(MBeanServer)→void、extends Serializable。
- ConstExp（apply 返回常量 Query.value(3)）组合：eq/gt/lt 真求值；plus(自定义,2).apply(null) toString "5"；apply 计数证明每次求值调用一次；setMBeanServer 回调被观察到。
- 序列化：Query.value(5)/value("q")/plus(2,3) round-trip 后值保持、apply 可再执行。
### 版本差异
- 无。
