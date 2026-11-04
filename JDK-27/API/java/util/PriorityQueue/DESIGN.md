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

## 契约深化追加（第二版）

新增 test 方法：testNullComparatorConstructors / testComparatorPlumbing / testCaseInsensitiveOrder /
testNonComparableEager / testRemovePositions / testInterleavedOfferPoll / testIteratorRemove /
testSerializationWithComparator / testNonSerializableComparator / testEmptyToArray /
testIaeMessageless / testWrongTypeQueries / testToStringBrackets / testHashCodeIdentity /
testElementIdempotent。

### 覆盖点

与 JDK-8 侧同构（null Comparator 构造不抛 NPE 走自然序、比较器实例 == 管道、remove 位置矩阵、
交错 offer/poll、iterator.remove、带比较器序列化同实例复原、不可序列化 Comparator →
NotSerializableException、空 toArray 复用+null 终止符、IAE 无消息、错误类型查询 false、
toString 括号、身份 hashCode、element 幂等），差异点：

- **非 Comparable 元素的立即 CCE（JDK-21+ 契约）**：**首个 offer 即抛 ClassCastException**
  （入队时转型 Comparable），队列不留元素；两元素集合构造 heapify 比较 CCE。
  （JDK-8 对照：首个 offer 成功、第二个才 CCE——两侧各自断言记录。）

### 易错规范细节（已锁入断言）

- Spliterator 特征位 JDK-8 == JDK-27（0x4140，黑盒段已断言 SIZED|SUBSIZED|NONNULL）。
- 序断言只走 poll 序列；toString/迭代序无契约。
