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
