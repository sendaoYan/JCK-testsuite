# Enum — 设计方案

## 1. API 功能
`java.lang.Enum<E>`（abstract）是所有枚举类型的超类：
- 名称/序号：`name()`（final）、`ordinal()`（final）
- 比较：`compareTo(E)`、`equals`（final，身份语义）、`hashCode`（final）
- 反射：`getDeclaringClass()`；工具：静态 `Enum.valueOf(Class,String)`
- 继承：实现 `Comparable`、`Constable`、`Serializable`

## 2. Java 用户如何使用
- 编译器为每个 `enum` 生成 `values()`/`valueOf(String)`；`name()`/`ordinal()` 从 `Enum` 继承。
```java
for (Suit s : Suit.values()) System.out.println(s.ordinal() + ":" + s.name());
Suit s = Enum.valueOf(Suit.class, "HEARTS");
```

## 3. 开源代码实际用法调研
- JDK 大量枚举（`Thread.State`、`Month`、`DayOfWeek`）继承 `Enum`。
- JDK `test/jdk/java/lang/Enum` 系列验证 `ordinal/name/compareTo/equals/getDeclaringClass` 与常量特定类体。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Enum")` 验证 abstract、父类 `Object`、实现 `Comparable`、`name/ordinal/equals/hashCode` final。
黑盒：`values/valueOf`（含异常）；`ordinal/name`；`compareTo/equals/hashCode`（身份语义）；`getDeclaringClass`；常量特定类体。
对应 `<Simple>Test.java` 黑盒部分。
