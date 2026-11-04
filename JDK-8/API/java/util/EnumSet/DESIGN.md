# java.util.EnumSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。allOf/noneOf/of/range/copyOf/complementOf 工厂（含边界：range(from==to)
> 单元素、range from>to IAE、range null NPE、complementOf(noneOf)==allOf / complementOf(allOf)==
> noneOf、of 变参重复去重、copyOf(空) IAE、copyOf(null) NPE、allOf/noneOf(null) NPE）、
> add/remove/contains、按枚举声明序（ordinal）迭代、与插入顺序无关、null 拒绝（add NPE、
> contains/remove(null) 优雅 false）、集合运算（addAll/removeAll/retainAll/containsAll）、
> equals/hashCode 顺序无关 + 跨实现（HashSet）、clone 深拷贝、序列化 round-trip + serialVersionUID、
> 状态不变量。

## 1. API 提供了哪些功能

- **抽象类**：`EnumSet`（无 public 构造器），通过静态工厂构造。
- **工厂（6 组）**：`allOf/noneOf`（Class）、`of`（1..5 元素 + varargs）、`range(from,to)`、
  `copyOf`（EnumSet/Collection）、`complementOf`（EnumSet）。
- **核心操作**：`add/remove/contains`、`size/isEmpty/clear`、迭代、集合运算、`clone`。
- **契约继承**：extends `AbstractSet`、implements `Cloneable`/`Serializable`
  （`serialVersionUID == 1009687484059888093L`）；`equals/hashCode` 顺序无关。

## 2. Java 用户如何使用

- `EnumSet.noneOf(Color.class)` → `add` 乱序 → 迭代按枚举声明序 → `of/range/copyOf/complementOf`
  构造 → `addAll/removeAll/retainAll` 集合运算 → `clone`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/EnumSet.html`：位向量实现、迭代序=声明序、null 拒绝。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/EnumSet.java`（工厂参数校验、
  range IAE、of 去重）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/EnumSet`。

## 4. 测试用例设计（映射到 EnumSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：工厂、增删迭代、null | testFactories、testAddRemoveIteration、testNullRejection |
| P0 | 工厂边界 | testFactoryEdgeCases |
| P0 | 集合运算 | testCollectionOps |
| P1 | clone 深拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（HashSet） | testCrossImplEquals |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 迭代顺序恒等于枚举声明序（ordinal），与插入顺序无关；`allOf` 也按声明序。
- 元素不能为 null：`add(null)` 抛 NPE；`contains(null)/remove(null)` 优雅返回 false。
- `of` 变参传重复元素**静默去重**（不抛异常）。
- `range(from, to)`：`from.ordinal() > to.ordinal()` 抛 IAE；from==to 单元素合法；
  任一参数 null 抛 NPE。
- `copyOf(空 Collection)` 抛 IAE；`copyOf(null)` 抛 NPE；`copyOf(EnumSet)` 保持元素。
- `complementOf(noneOf)==allOf`，`complementOf(allOf)==noneOf`（补集对合）。
- `equals/hashCode` 与顺序无关，与 `HashSet` 内容相同时互相 equals、hashCode 相等。
- `clone` 是深拷贝（位向量复制），与原集合结构独立。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
