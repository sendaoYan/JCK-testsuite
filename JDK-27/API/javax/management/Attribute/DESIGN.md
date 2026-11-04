# javax.management.Attribute 黑盒行为测试设计

## 1. API 功能
- 构造：`Attribute(String name, Object value)`，name 不能为 null。
- 查询：`getName()`、`getValue()`。
- 契约：`equals`/`hashCode` 基于 name+value；`toString` 包含二者。

## 2. Java 用户如何使用
MBeanServer 的 `getAttribute`/`setAttribute` 用 Attribute 打包属性名和值；用户构造后传给 setAttribute 或从 getAttributes 结果里拆包。
```java
mbs.setAttribute(on, new Attribute("Count", 42));
Attribute a = (Attribute) mbs.getAttribute(on, "Count");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/AttributeListTypeSafeTest.java`：Attribute/AttributeList 的用法。
- `test/jdk/javax/management/Introspector/*Test.java`：属性读写路径。
- 实现权威：`src/java.management/share/classes/javax/management/Attribute.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getName/getValue 返回构造入参；equals/hashCode 对称且对 name/value 敏感；null value 合法且相等性成立；null name 抛 RuntimeOperationsException（源码确认包裹 IllegalArgumentException）。
- 白盒：public 具体类、implements Serializable、含 getName/getValue/equals/hashCode/toString。
