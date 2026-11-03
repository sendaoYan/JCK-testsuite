# javax.management.AttributeValueExp 黑盒行为测试设计

## 1. API 功能
- 实现 `ValueExp`。
- 方法：`getAttributeName()`、`apply(ObjectName)`、`setMBeanServer(MBeanServer)`、`toString()`。

## 2. Java 用户如何使用
与 `Query` 组合表达“属性名等于某值”的查询；直接 apply 会在当前 QueryEval 的 MBeanServer 上取该属性值。
```java
ValueExp e = new AttributeValueExp("Count");
ValueExp v = e.apply(objectName);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/QueryFactory.java`：Query.attr 内部使用。
- `test/jdk/javax/management/query/SupportedQueryTypesTest.java`：apply 结果类型。
- 实现权威：`src/java.management/share/classes/javax/management/AttributeValueExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getAttributeName/toString 返回属性名；apply 对 String 属性返回 StringValueExp、对 Number 属性返回 ValueExp；缺失属性因取到 null 抛 BadAttributeValueExpException（源码确认）。
- 白盒：public 类、implements ValueExp、含 apply/setMBeanServer。
