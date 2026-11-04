# java.util.HashSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（负容量/NaN 负载因子/null Collection）、null 元素语义、
> 迭代器写通道（iterator.remove/removeAll/retainAll/clear/removeIf）、fail-fast（CME/ISE/NSEE）、
> toArray 契约、跨实现 equals（vs TreeSet）、clone 浅拷贝、序列化 round-trip、spliterator 特征、
> 状态不变量全部补齐。HashSet 无序，断言均排序后比较或依赖内容相等。

## 1. API 提供了哪些功能

- **构造器（4）**：`HashSet()`、`HashSet(int)`、`HashSet(int,float)`、`HashSet(Collection)`。
- **核心操作**：`add`（重复返 false）、`remove`、`contains`、`size`、`isEmpty`、`clear`、
  `addAll/removeAll/retainAll/containsAll`、`removeIf`。
- **契约继承**：`clone()`（浅拷贝）、`Serializable`（`serialVersionUID == -5024744406713321676L`）、
  `Set.equals/hashCode`（内容相等、顺序无关）、`spliterator()`（SIZED|DISTINCT）。

## 2. Java 用户如何使用

- `new HashSet<>()` 建集合 → `add` 填充（含 null）→ `contains/remove` 查询删除 →
  集合运算 → `toArray` → `iterator` 遍历/删除。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/HashSet.html`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/HashSet`（`Serialization`、`ToArray` 等）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/HashSet`。

## 4. 测试用例设计（映射到 HashSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：负容量/NaN/负/0 负载因子→IAE、null Collection→NPE、容量 0 合法 | testConstructorMatrix |
| P0 | null 元素语义：add/contains/remove(null) | testNullSemantics |
| P0 | 迭代器写通道 + ISE/NSEE | testIteratorWriteChannels |
| P0 | fail-fast：add/remove/clear 后迭代 CME | testFailFast |
| P0 | 跨实现 equals：HashSet vs TreeSet 对称 + hashCode | testCrossImplEquals |
| P1 | toArray 契约：复用 ==、null 终止符、精确长度、多重集 | testToArrayContract |
| P1 | clone 浅拷贝 + 结构独立 | testClone |
| P1 | 序列化 round-trip（含 null）+ serialVersionUID | testSerialization |
| P1 | spliterator SIZED|DISTINCT + 全量覆盖 | testSpliterator |
| P1 | 状态不变量：删除/失败操作后其余元素完整、clear 后逐元素验证 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `HashSet(int)` 负容量→IAE；`HashSet(int,float)` 负载因子 ≤0 或 NaN→IAE；容量 0 合法可用。
- 迭代器 `remove` 前未 `next` 抛 ISE；耗尽后 `next` 抛 NSEE。
- `toArray(T[])` 容量足够时复用同一数组、多余位补 null 终止符。
- HashSet 无序：toArray/迭代/forEach 断言均排序后比较。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
