# javax.management.StringValueExp 黑盒行为测试设计

## 1. API 功能
- 实现 ValueExp：`getValue()`、`apply(ObjectName)`、`setMBeanServer(MBeanServer)`、`toString()`。

## 2. Java 用户如何使用
代表字符串常量值的 ValueExp，通常由 Query.value(String) 创建。
```java
ValueExp v = new StringValueExp("hello");
String s = ((StringValueExp) v).getValue();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/QueryFactory.java`。
- 实现权威：`src/java.management/share/classes/javax/management/StringValueExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getValue 返回构造字符串；toString 返回 value；apply 返回 this（或等价 StringValueExp）；setMBeanServer 不抛异常。
- 白盒：public 类、implements ValueExp、implements Serializable、含 getValue/apply。
