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

## 深化批次 II（契约级追加）

- **hashCode 公式锁定**（同 ArrayList：128/4066/126046）。
- **Deque 12 方法头尾矩阵**：空态 peek/poll 族返回 null、get/remove 族抛 NSEE；非空态各方法只作用于对应端。
- **Queue/Deque 别名等价**：add≡addLast、remove≡removeFirst、element≡getFirst、peek≡peekFirst、
  poll≡pollFirst、push≡addFirst、pop≡removeFirst（平行状态对照断言）。
- **ListIterator 状态机**：add 后 set/remove→ISE；previous 后 set/remove 合法作用于刚返回元素；
  nextIndex/previousIndex 全矩阵（含 add 后游标 +1）。
- **descendingIterator**：逆序遍历、remove 删除已访问尾元素、双 remove ISE、空迭代器 NSEE、forEachRemaining。
- **subList 契约**：equals/hashCode 按自身内容；add/remove 透传；父 removeFirst 后子表 CME；addAll 头/中/尾。
- **iterator.remove 过滤范式**：迭代中删除非 3 倍数剩 [3,6]。
