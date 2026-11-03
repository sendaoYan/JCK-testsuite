# javax.management.Query 黑盒行为测试设计

## 1. API 功能
- 比较常量：GT/LT/GE/LE/EQ；算术常量：PLUS/MINUS/TIMES/DIV。
- 工厂方法：`eq/gt/lt/ge/leq/and/or/not/between/in/match/initialSubString/anySubString/finalSubString/isInstanceOf`，`attr/value/plus/minus/times/div/classattr`。

## 2. Java 用户如何使用
构造 QueryExp 交给 MBeanServer.queryNames/queryMBeans 做条件查询。
```java
QueryExp q = Query.and(Query.gt(Query.attr("Count"), Query.value(0)),
                       Query.attr("Name").equals(Query.value("c*")));
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/QueryFactory.java`、`QueryMatchTest.java`、`QuerySubstringTest.java`、`InstanceOfExpTest.java`、`SupportedQueryTypesTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/Query.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：各工厂返回非空 QueryExp/ValueExp；常量值正确；把组合查询用于已注册 StandardMBean，验证 eq/gt/lt/and/or/not/between/in/match/子串/isInstanceOf 的匹配结果。
- 白盒：public 类、public 常量、含各工厂方法。
