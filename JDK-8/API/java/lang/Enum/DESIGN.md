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
## 契约深化（2026-11）

### API 功能（新增覆盖维度）
自造五类枚举（Planet 字段+带参构造+派生方法、Op 常量体实现抽象方法、Severity 实现接口、Color 简单枚举、Nothing 空 enum、Labeled 覆写 toString）：values() 防御拷贝、valueOf 精确异常消息、compareTo 原始差值与 null/跨枚举负例、序列化单例保持、switch 全分支、Class.isEnum 层面、编译器合成成员白盒。

### 使用方式
`Color.values()` 每次返回新数组（改元素不影响后续调用）；`Color.valueOf("NOPE")` 抛 IAE，消息逐字 `No enum constant EnumTest.Color.NOPE`（**用的是 getCanonicalName()，嵌套枚举为点分形式，非 getName() 的 $ 形式**）。

### 参考来源
JLS 8.9（枚举）；Enum.valueOf javadoc（IllegalArgumentException "No enum constant ..."）；java.io.Enum 序列化规范（枚举按 name 序列化，svuid 固定 0）。

### 测试用例设计 + 易错规范细节
- **compareTo 返回原始 ordinal 差**：`RED.compareTo(BLUE) == -2`（非 Integer.compare 的归一化 -1）。
- **compareTo(null) → NPE**：JDK-8 消息为 `null`（getMessage()==null），JDK-14+ 为 helpful message（本侧 MODERN=false 走 null 分支）。
- **Enum.valueOf(cls, null) → NPE 消息两侧一致 "Name is null"**；Enum.valueOf(null, name) 8 侧消息 null。
- **跨枚举 compareTo（raw 类型）→ ClassCastException**。
- **values() 防御拷贝**：两次调用返回不同数组引用；置空元素后再调 values() 完好。
- **序列化往返 == 原常量**（单例保持，与 record 的 equals 语义相对）；`ObjectStreamClass.lookup(任意枚举).getSerialVersionUID() == 0L`。
- **常量体 getDeclaringClass() == 枚举自身**（getClass() 是匿名子类，二者对照锁定）。
- 覆写 toString 不影响 name()；默认 toString == name()。
- 空 enum values().length==0 且 valueOf 负例消息同模板。
- Enum.class.isEnum() == false（Enum 本身不是枚举）；String/Thread.State 对照。
- 白盒：Enum.clone()/finalize() protected final、toString 非 final（可覆写）、唯一 protected Enum(String,int) 构造器、编译器合成 static values()/valueOf(String)。
- EnumTest.java 现有 PASS 断言：172。
