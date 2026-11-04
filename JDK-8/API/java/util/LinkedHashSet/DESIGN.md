# java.util.LinkedHashSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。无重复 + 插入序迭代（重复 add 拒绝且不重排）、构造器矩阵（负容量/
> loadFactor≤0/NaN IAE、null Collection NPE、拷贝保留顺序）、null 元素允许、clone 浅拷贝、
> 序列化 round-trip + serialVersionUID、equals/hashCode 顺序无关 + 跨实现（HashSet）、状态不变量。

## 1. API 提供了哪些功能

- **构造器（4）**：`LinkedHashSet()`、`LinkedHashSet(int)`、`LinkedHashSet(int,float)`、
  `LinkedHashSet(Collection)`。
- **核心操作**：`add/remove/contains`（继承 HashSet，元素唯一），保持插入序迭代。
- **契约继承**：extends `HashSet`、implements `Set`/`Cloneable`/`Serializable`
  （`serialVersionUID == -2851667679971038690L`）；`equals/hashCode` 顺序无关（Set 契约）。

## 2. Java 用户如何使用

- `new LinkedHashSet<>()` 建保留插入序的集合 → `add` 去重 → 按插入序遍历 → `remove/contains`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/LinkedHashSet.html`：插入序、null 允许。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/LinkedHashSet.java`（构造器
  IAE/NPE、基于 LinkedHashMap 实现）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/LinkedHashSet`。

## 4. 测试用例设计（映射到 LinkedHashSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：无重复 + 插入序 | testOrderAndDuplicates |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | null 元素允许 | testNullAllowance |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（HashSet） | testCrossImplEquals |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 迭代顺序 == 插入顺序；重复 `add` 返回 false 且不改变位置；null 元素保持插入位置。
- 构造器：`LinkedHashSet(-1)`、`loadFactor≤0 或 NaN` 抛 IAE；`LinkedHashSet(null Collection)` 抛 NPE；
  `LinkedHashSet(Collection)` 保留源集合的相遇顺序。
- `equals/hashCode` 与顺序无关，与 `HashSet` 内容相同时互相 equals、hashCode 相等。
- `clone` 浅拷贝，与原集合结构独立。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
