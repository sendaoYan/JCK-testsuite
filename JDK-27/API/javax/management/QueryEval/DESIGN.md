# javax.management.QueryEval 黑盒行为测试设计

## 1. API 功能
- 抽象类，实现 ValueExp/QueryExp 的基类。
- 静态/实例方法：`getMBeanServer()`（thread-local）、`setMBeanServer(MBeanServer)`；`apply(ObjectName)`。

## 2. Java 用户如何使用
Query.attr/value 产生的表达式继承它，在 apply 时从 thread-local 取 MBeanServer 解析属性。
```java
QueryEval eval = ...;
eval.setMBeanServer(mbs);
ValueExp v = ((QueryEval) attrExp).apply(on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/SupportedQueryTypesTest.java`、`CustomQueryTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/QueryEval.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：匿名子类 setMBeanServer 后 getMBeanServer 返回同一服务器；线程局部隔离；配合 AttributeValueExp.apply 解析属性。
- 白盒：public 抽象类、implements Serializable、含 getMBeanServer/setMBeanServer。
