# java.util.ArrayDeque 测试设计案（JDK-27）

> 覆盖目标：**完整契约**，白盒（反射结构）+ 黑盒（行为契约）。既有白盒/黑盒已覆盖
> Queue/Deque 双端、push/pop、removeFirst/LastOccurrence、null 拒绝、迭代器顺序、扩容、
> forEach/spliterator/removeAll/retainAll/removeIf/clear；本版补齐构造器矩阵、空语义、
> toArray 契约、序列化 round-trip + serialVersionUID、状态不变量。
> **fail-fast 不做断言**：JDK-27 的 ArrayDeque 迭代器对 addLast/removeLast/addFirst 的
> modCount 检测与 JDK-8 不同（实证不抛 CME），跨版本无稳定契约，故本测试不锁定。

## 1. API 提供了哪些功能

- 与 JDK-8 相同：构造器（3）、Queue 操作、Deque 操作、`Cloneable`/`Serializable`
  （`serialVersionUID == 2340985798034038923L`）、`equals/hashCode` 身份语义。
- JDK-27 额外默认方法（既有黑盒已覆盖）：`forEach`、`removeIf`、`spliterator`
  （ORDERED|NONNULL|SIZED|SUBSIZED）。

## 2. Java 用户如何使用

- 与 JDK-8 一致：队列/栈双端操作、双向遍历。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/ArrayDeque.html`（JDK-8 与 JDK-27）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/ArrayDeque` + `~/git/jdk/test/jdk/java/util/ArrayDeque`。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/ArrayDeque`。

## 4. 测试用例设计（映射到 ArrayDequeTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒结构：继承 AbstractCollection、实现 Deque/Cloneable/Serializable、成员形态 | testWhiteBox |
| P0 | 既有黑盒：Queue/Deque/push-pop/occurrences/null/迭代器/扩容/forEach/removeAll/retainAll/removeIf/spliterator/clear | testQueue、testDeque、testPushPop、testRemoveOccurrences、testNullRejection、testIterators、testGrowth、testBlackBox |
| P0 | 构造器矩阵 + 空语义 | testConstructorMatrix、testEmptySemantics |
| P1 | toArray 契约、序列化、状态不变量 | testToArrayContract、testSerialization、testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 同 JDK-8：`ArrayDeque(int)` 对负/0 容量不抛（最小容量 8）；null 集合/含 null 集合 NPE；
  add/offer 族传 null NPE；contains/remove(null) 返 false；空时 poll/peek 族返 null、
  remove/element/getFirst/getLast/removeFirst/removeLast/pop 抛 NSEE；身份 equals。
- **fail-fast 跨版本无稳定契约**：JDK-27 不抛 CME，测试不锁定（与 JDK-8 版差异点）。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
