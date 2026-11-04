# java.util.PriorityQueue 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（负/0 容量 IAE、null 参数 NPE、含 null 集合 NPE）、
> null 语义（add/offer NPE；contains/remove(null) 返 false）、空 vs 异常语义（element/remove NSEE、
> poll/peek null）、重复元素、toArray 契约、迭代器 fail-fast、身份 equals（AbstractQueue 不覆写）、
> 序列化 round-trip、状态不变量全部补齐。堆序断言只依赖 poll 序列（单调），不依赖内部数组顺序。

## 1. API 提供了哪些功能

- **构造器（7）**：`PriorityQueue()`、`PriorityQueue(int)`、`PriorityQueue(int,Comparator)`、
  `PriorityQueue(Comparator)`、`PriorityQueue(Collection)`、`PriorityQueue(PriorityQueue)`、
  `PriorityQueue(SortedSet)`。
- **核心操作**：`add/offer`（永不失败）、`peek/poll`（最小/最优先出队）、`element/remove()`（空抛 NSEE）、
  `remove(Object)/contains`、`removeIf/removeAll/retainAll`、`clear`、`comparator()`。
- **契约继承**：`Serializable`（`serialVersionUID == -7720805057305804111L`）；`equals` 未覆写（身份语义）。

## 2. Java 用户如何使用

- `new PriorityQueue<>(Comparator)` 建堆 → `add/offer` 填充 → `peek` 看最小 → `poll` 依序取出 →
  `remove/removeIf` 定向删除 → `contains` 查询 → `comparator()` 查看比较器。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/PriorityQueue.html`：容量 ≥1、null 拒绝、heap 语义。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/PriorityQueue`（`RemoveContains`、`AddNull`、
  `NoElement` 等）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/PriorityQueue`。

## 4. 测试用例设计（映射到 PriorityQueueTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：负/0 容量 IAE、null Collection/SortedSet NPE、含 null 集合 NPE、SortedSet/PriorityQueue 拷贝、comparator 保留 | testConstructorMatrix |
| P0 | null 语义：add/offer(null) NPE；contains(null)/remove(null) 返 false | testNullSemantics |
| P0 | 空 vs 异常语义 + 重复元素 | testEmptySemantics、testDuplicates |
| P0 | toArray 契约：复用 ==、null 终止符、多重集 | testToArrayContract |
| P0 | 迭代器 fail-fast：add/remove 后 CME | testFailFast |
| P0 | 身份 equals：PriorityQueue 不覆写 equals | testIdentityEquals |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 状态不变量：删除/失败操作后其余元素完整、clear 后逐元素验证 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `PriorityQueue(int)` 初始容量 **必须 ≥1**（JDK-8 与 JDK-27 均如此），0/负 → IAE。
- `contains(null)`/`remove(null)` 返回 false（不抛 NPE）；`add/offer(null)` 抛 NPE。
- `element()`/`remove()` 空抛 NSEE；`poll()/peek()` 空返 null。
- `PriorityQueue` 继承 `AbstractQueue`，**不覆写 equals**：内容相同的两个队列 `equals` 仍为 false（身份）。
- 迭代顺序不保证有序，仅保证 poll 序列单调。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 契约深化追加（第二版）

新增 test 方法：testNullComparatorConstructors / testComparatorPlumbing / testCaseInsensitiveOrder /
testNonComparableJdk8 / testRemovePositions / testInterleavedOfferPoll / testIteratorRemove /
testSerializationWithComparator / testNonSerializableComparator / testSpliterator /
testEmptyToArray / testIaeMessageless / testWrongTypeQueries / testToStringBrackets /
testHashCodeIdentity / testElementIdempotent。

### 覆盖点

- **null Comparator 构造**：`new PriorityQueue((Comparator)null)` 与 `(8, null)` 均**不抛 NPE**，
  语义为自然序（comparator()==null）——非直觉契约，实测锁定。
- Comparator 管道：PQ(Comparator)/PQ(SortedSet)/PQ(PriorityQueue) 三构造均保留**同一比较器实例**（==）；
  自然序 TreeSet 构造 comparator()==null。
- **非 Comparable 元素的惰性 CCE（JDK-8 专属）**：首个 offer 不比较（无父节点）成功入队；
  第二个 offer 比较时 CCE；两元素集合构造 CCE。**JDK-27 为首个 offer 立即 CCE（版本差异）。**
- remove 位置矩阵：删头（min）→ 次小浮现；删 max → peek 不变；删中间；全排干。
- offer/poll 交错重建堆（5→poll→3,1→poll 1,3）；iterator.remove 受支持且堆仍正确排干。
- 序列化带比较器：String.CASE_INSENSITIVE_ORDER round-trip 后 `comparator()==同一实例`（readResolve
  共享静态）；**不可序列化 Comparator → ObjectOutputStream 直接抛 NotSerializableException**。
- Spliterator：特征位 **0x4140 = SIZED|SUBSIZED|NONNULL**（无 ORDERED/SORTED——堆无序契约）、
  trySplit 尺寸和守恒 + multiset 并集。
- 空 toArray(T[])：复用数组且 [0]=null 终止符；PQ(0)/PQ(-3) IAE **无 detail message**（getMessage()==null）。
- 错误类型查询 contains(42)/remove(42) 返 false 不抛 CCE；toString 只断括号+含元素（迭代序无契约）；
  hashCode==identityHashCode；element() 幂等不删。

### 易错规范细节（已锁入断言）

- CASE_INSENSITIVE_ORDER 下大小写不同串相等——并列元素只断言 multiset，不断言先后。
- toString/迭代顺序是堆数组序（无契约），凡序断言一律走 poll 序列。
