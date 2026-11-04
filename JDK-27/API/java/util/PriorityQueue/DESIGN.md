# java.util.PriorityQueue 测试设计案（JDK-27）

> 覆盖目标：**完整契约**，白盒（反射结构）+ 黑盒（行为契约）。构造器矩阵、null 语义、
> 迭代器 fail-fast、身份 equals、toArray 契约、序列化 round-trip、状态不变量补齐；
> 白盒/黑盒既有内容已覆盖堆序、重复、空语义、remove/removeIf、comparator 访问、集合构造。

## 1. API 提供了哪些功能

- **构造器（7）**：`PriorityQueue()`、`PriorityQueue(int)`、`PriorityQueue(int,Comparator)`、
  `PriorityQueue(Comparator)`、`PriorityQueue(Collection)`、`PriorityQueue(PriorityQueue)`、
  `PriorityQueue(SortedSet)`。
- **核心操作**：同 JDK-8。
- **契约继承**：`Serializable`（`serialVersionUID == -7720805057305804111L`）；`equals` 未覆写。

## 2. Java 用户如何使用

- 与 JDK-8 一致。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/PriorityQueue.html`（JDK-8 与 JDK-27）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/PriorityQueue` + `~/git/jdk/test/jdk/java/util/PriorityQueue`。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/PriorityQueue`。

## 4. 测试用例设计（映射到 PriorityQueueTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒结构：继承 AbstractQueue、实现 Serializable、成员形态 | testWhiteBox |
| P0 | 既有黑盒：堆序/重复/空语义/remove/removeIf/集合构造/comparator | testNaturalOrder、testComparatorOrder、testDuplicates、testElementRemoveEmpty、testRemoveContainsRemoveIf、testConstructorFromCollectionAndSortedSet、testBlackBox |
| P0 | 构造器矩阵 + null 语义 + fail-fast + 身份 equals | testConstructorMatrix、testNullSemantics、testFailFast、testIdentityEquals |
| P1 | toArray 契约、序列化、状态不变量 | testToArrayContract、testSerialization、testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 同 JDK-8 版：初始容量 ≥1；add/offer(null) NPE；contains/remove(null) 返 false；equals 身份语义。

## 注意事项

- 白盒反射只断言公开结构；serialVersionUID 与 JDK-8 相同。
