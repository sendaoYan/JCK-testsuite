# javax.management.ObjectName 黑盒行为测试设计

## 1. API 功能
- 构造/工厂：多种重载与 `getInstance`。
- 查询：getDomain/getKeyProperty/getKeyPropertyList/getCanonicalName/getCanonicalKeyPropertyListString。
- 谓词：isPattern/isDomainPattern/isPropertyPattern/isPropertyListPattern/isPropertyValuePattern。
- 匹配：`apply(ObjectName)`、`compareTo`；`quote/unquote`。

## 2. Java 用户如何使用
命名并定位 MBean；用模式（`*`、`?`）批量查询。
```java
ObjectName on = new ObjectName("com.example:type=Cache,name=1");
Set<ObjectName> s = mbs.queryNames(new ObjectName("com.example:type=Cache,*"), null);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/ObjectName/ApplyWildcardTest.java`、`ValueWildcardTest.java`、`ComparatorTest.java`、`NullEmptyKeyValueTest.java`、`ObjectNameGetInstanceTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/ObjectName.java`；通配匹配见 `com/sun/jmx/mbeanserver/Util.java`（仅 `*`、`?`，无 `[...]` 字符类）。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造与 getter、canonical 排序、equals/hashCode、各种 pattern 谓词、apply 匹配（含 `?`、`*`、无字符类）、compareTo、quote/unquote、MalformedObjectNameException 的精确消息。
- 白盒：public 类、implements Comparable、implements QueryExp、WILDCARD 存在。
