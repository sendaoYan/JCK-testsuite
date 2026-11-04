# java.lang.Enum 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「读个枚举常量」的冒烟。重点补：白盒结构（abstract、Object 超类、
> 实现 Comparable 与 Serializable、name/ordinal/equals/hashCode/compareTo 均 final）、
> values/valueOf（含 IllegalArgumentException/NPE）、ordinal/name/toString、compareTo 按声明序、
> equals/hashCode 身份语义、getDeclaringClass、常量特定类体（抽象成员 + 独立子类）。
> 断言确定，headless 安全。

## 1. API 提供了哪些功能

- **抽象类**：`java.lang.Enum<E extends Enum<E>>` 是所有枚举类型的超类。
- **名称/序号**：`name()`（final）、`ordinal()`（final）。
- **比较**：`compareTo(E)`（final，按声明序）、`equals`（final，身份语义）、`hashCode`（final）。
- **反射**：`getDeclaringClass()`；工具：静态 `Enum.valueOf(Class,String)`。
- **继承**：实现 `Comparable`、`Serializable`。

## 2. Java 用户如何使用

- 编译器为每个 `enum` 生成 `values()`/`valueOf(String)`；`name()`/`ordinal()` 从 `Enum` 继承。
- `Enum.valueOf(Suit.class, "HEARTS")` 按名查找；`valueOf` 未知名抛 `IllegalArgumentException`、
  null 抛 `NullPointerException`。
- 常量特定类体可覆写/实现抽象成员（如运算符枚举）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Enum.html`）：name/ordinal/compareTo/equals/hashCode/
  getDeclaringClass 契约、valueOf 异常。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Enum.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Enum`。

## 4. 测试用例设计（映射到 EnumTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Object 超类、Comparable+Serializable、name/ordinal/equals/hashCode/compareTo final | testWhiteBox |
| P0 | values() 声明序 + valueOf（含 IAE/NPE） + Enum.valueOf(Class,String) | testValuesAndValueOf |
| P0 | ordinal/name/默认 toString==name | testOrdinalName |
| P0 | compareTo 声明序 + equals/hashCode 身份语义 | testCompareEqualsHashCode |
| P1 | getDeclaringClass/getClass/instanceof | testDeclaringClass |
| P1 | 常量特定类体（字段初始化、抽象方法、独立子类） | testConstantSpecificBody |

## 关键规范细节（易错，已锁入断言）

- **`valueOf` 未知名抛 `IllegalArgumentException`、null 抛 `NullPointerException`**。
- **`equals`/`hashCode` 是 final 且身份语义**（`hashCode` == `System.identityHashCode`）。
- **`compareTo` 按 `ordinal`（声明序）**，不可覆写（final）。
- **默认 `toString() == name()`**（除非枚举覆写 toString）。
- **常量特定类体**：带抽象成员/覆写的常量有独立的匿名子类（`getClass() != 枚举类`）。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不涉及 JDK-12+ 的 `describeConstable()`。
