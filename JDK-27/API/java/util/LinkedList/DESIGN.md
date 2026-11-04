# java.util.LinkedList 测试设计案（JDK-27）

> 覆盖目标：**完整契约**，白盒（反射结构）+ 黑盒（行为契约）。构造器矩阵、listIterator 契约 +
> ISE、listIterator fail-fast、subList 活视图、跨实现 equals、序列化 round-trip、状态不变量补齐；
> 白盒/黑盒既有内容已覆盖 Queue/Deque 语义、null 容忍、removeFirst/LastOccurrence、
> descendingIterator、JDK 21+ `reversed()` 视图与 spliterator/clone。

## 1. API 提供了哪些功能

- **构造器（2）**：`LinkedList()`、`LinkedList(Collection)`。
- **List/Queue/Deque 操作**：同 JDK-8。
- **JDK 21+ SequencedCollection**：`reversed()` 倒序活视图、`addFirst/addLast/getFirst/getLast`、
  `removeFirst/removeLast`。
- **契约继承**：`Cloneable`、`Serializable`（`serialVersionUID == 876323262645176354L`）。

## 2. Java 用户如何使用

- 与 JDK-8 一致；JDK 21+ 可用 `reversed()` 获取倒序视图。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/LinkedList.html`（JDK-8 与 JDK-27）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/LinkedList` + `~/git/jdk/test/jdk/java/util/LinkedList`。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/LinkedList`。

## 4. 测试用例设计（映射到 LinkedListTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒结构：继承 AbstractSequentialList、实现 List/Deque/Cloneable/Serializable | testWhiteBox |
| P0 | 既有黑盒：List 契约链、Queue/Deque 语义、removeOccurrences、null 容忍、push/pop、descending/listIterator fail-fast | testContractChain、testQueueSemantics、testDeque、testRemoveOccurrences、testNullTolerance、testStackPushPop、testDescendingListIteratorFailFast |
| P0 | 构造器矩阵 + listIterator 契约/ISE + listIterator fail-fast | testConstructorMatrix、testListIteratorContract、testListIteratorFailFast |
| P0 | subList 活视图 + 跨实现 equals | testSubListView、testCrossImplEquals |
| P1 | 序列化 round-trip + serialVersionUID + 状态不变量 | testSerialization、testStateInvariants |
| P1 | JDK 21+ reversed() 倒序活视图 | testBlackBox（已含） |

## 关键规范细节（易错，已锁入断言）

- listIterator `set/remove` 前未 `next/previous` 抛 ISE；listIterator 结构修改后 next 抛 CME。
- `reversed()`（JDK 21+）返回倒序活视图，写入联动到原表。

## 注意事项

- 白盒反射只断言公开结构；serialVersionUID 与 JDK-8 相同。
