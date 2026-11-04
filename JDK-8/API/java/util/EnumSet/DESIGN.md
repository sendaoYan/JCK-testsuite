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

## 契约深化补充（第二轮：of 重载与 null/copyOf 类型边界/addAll 类型边界/空枚举/迭代器通道/跨类型 equals/varargs 别名/批量返回值/精确特征位/巨型枚举/深度序列化）

新增方法（只追加）：

| 测试点 | 用例方法 |
|---|---|
| of(E)~of(E,E,E,E,E) 全重载 + of(null)/of(e,null 数组)/of(e,{...,null}) NPE | testOfAritiesAndNull |
| copyOf(Collection) 类型边界：错误元素类型 CCE、含 null NPE、从 HashSet 推断枚举类型、结果声明序 | testCopyOfTyping |
| addAll 类型边界：普通 List 可加、错误类型 CCE、含 null NPE | testAddAllTyping |
| 空枚举宇宙：noneOf/allOf(空枚举) 空、迭代器空、copyOf(空) IAE | testEmptyEnumUniverse |
| 迭代器通道：从最小 ordinal 起、remove 保序、fresh remove ISE、forEachRemaining 剩余声明序；**EnumSet 迭代器非 fail-fast**（快照掩码：并发 add 不抛 CME 且不可见，双侧一致） | testIteratorChannels |
| 跨枚举类型 equals false | testCrossTypeEquals |
| of(first, rest...) 不别名调用方数组 | testVarargsNotAliased |
| 批量返回值：全重复 addAll/无命中 removeAll/超集 retainAll/无命中 removeIf false；containsAll(empty) true；null NPE×4 | testBatchReturnValues |
| **JDK-8 精确特征位 == SIZED\|SUBSIZED\|DISTINCT（0x4041）**；无 SORTED → getComparator() 抛 IllegalStateException | testSpliteratorExact |
| >64 常量（Jumbo/HugeEnumSet 路径）：range 端点、声明序迭代、iterator.remove、complementOf 双补还原 | testHugeEnumUniverse |
| 序列化：声明序保持、round-trip 后 add 仍按序、空集/巨型枚举 round-trip | testSerializationDeep |

负例精确异常表（新锁定）：
- add(null)/of(null)/range(任一 null)/allOf(null)/noneOf(null)/complementOf(null)/copyOf(null) → NPE
- contains(null)/remove(null) → false（不抛）
- copyOf(空 Collection) → IAE；copyOf/copyOf 非枚举元素 → CCE；addAll 错误类型 → CCE、含 null → NPE
- range(from>to) → IAE
- spliterator.getComparator()（JDK-8 无 SORTED）→ IllegalStateException

JDK-8 vs JDK-27 差异（已各自锁定）：EnumSet.spliterator() 特征位 JDK-8 为 0x4041（SIZED|SUBSIZED|DISTINCT，无 ORDERED/SORTED/NONNULL，getComparator 抛 ISE），JDK 9+ 扩为 0x4155（加 ORDERED|SORTED|NONNULL，getComparator 返回 null）；其余契约双侧一致（迭代器均非 fail-fast、掩码快照语义相同）。
