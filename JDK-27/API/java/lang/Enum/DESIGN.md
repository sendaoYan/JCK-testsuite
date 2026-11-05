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
## 契约深化（2026-11）

### API 功能（新增覆盖维度）
与 JDK-8 侧同构：自造 Planet/Op/Severity/Color/Nothing/Labeled 六类枚举全维度契约；另含 12+ 侧 describeConstable/EnumDesc 契约（常量体枚举的 EnumDesc.constantName、constantName 经 valueOf round-trip）。

### 使用方式
同 JDK-8 侧；27 侧 NPE 消息走 helpful 分支：`compareTo(null)` → `Cannot invoke "Object.getClass()" because "other" is null`；`Enum.valueOf(null,name)` → `Cannot invoke "java.lang.Class.enumConstantDirectory()" because "enumClass" is null`。

### 参考来源
JLS 8.9；JEP 334（Constable/EnumDesc，12+）。

### 测试用例设计 + 易错规范细节
- 与 8 侧一致的模板断言（valueOf IAE 消息用 canonical name、compareTo 原始差值、序列化 == 单例、svuid 0L、防御拷贝、常量体 getDeclaringClass、Enum.class.isEnum()==false）。
- **JDK-8 vs JDK-27 差异（实测锁定）**：NPE helpful message（8 侧 getMessage()==null）；27 侧 Enum 实现 Constable（describeConstable/EnumDesc 仅 27 可测）。
- EnumTest.java 现有 PASS 断言：179。
