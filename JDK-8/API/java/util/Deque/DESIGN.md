# java.util.Deque 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。双端 add/offer/get/peek/remove/poll、push/pop（LIFO）、
> removeFirstOccurrence/removeLastOccurrence、升/降序迭代器、空双端队列 null vs NSEE、
> null 参数 NPE。

## 1. API 提供了哪些功能

- **双端增查删**：`addFirst/addLast/offerFirst/offerLast`、`getFirst/getLast/peekFirst/peekLast`、
  `removeFirst/removeLast/pollFirst/pollLast`。
- **栈语义**：`push`（头部入）/`pop`（头部出，空抛 NSEE）。
- **按值移除**：`removeFirstOccurrence`/`removeLastOccurrence`。
- **迭代**：`iterator`（头到尾）、`descendingIterator`（尾到头）。

## 2. Java 用户如何使用

- 双端队列可当队列（FIFO）也可当栈（LIFO）使用；`offerFirst/offerLast` 无界下恒真；
  需要按值去重时用 `removeFirstOccurrence`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Deque.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/ArrayDeque.java`。

## 4. 测试用例设计（映射到 DequeTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 双端增查 + null NPE | testAddGet |
| P0 | 双端删/空 NSEE + poll null | testRemovePoll |
| P0 | push/pop + 按值移除 | testStackAndOccurrence |
| P0 | 升/降序迭代器 + Queue/Collection 契约 | testIterators |

## 关键规范细节（易错，已锁入断言）

- 空双端队列：`pollFirst/pollLast/peekFirst/peekLast` 返回 **null**；`removeFirst/removeLast/
  getFirst/getLast/pop` 抛 **NoSuchElementException**。
- ArrayDeque 拒绝 null 元素：`addFirst(null)`/`offerLast(null)` 抛 **NPE**；
  而 `removeFirstOccurrence(null)` 在 JDK-8 显式短路返回 **false**（不抛异常）。

## 注意事项

- 用 ArrayDeque（无界、拒 null、有序）为载体；断言依赖其头尾顺序。

## 契约深化追加（第二版）

新增 test 方法：testTwelveMethodMatrix / testQueueViewAliasing / testOccurrencesDeep /
testBulkOperations / testToArrayContract / testIteratorRemoveSemantics / testFailFastCme /
testNullMatrix / testAlternatingDrain。

### 覆盖点

- **头尾 12 方法矩阵**：addFirst/addLast/offerFirst/offerLast/removeFirst/removeLast/pollFirst/
  pollLast/getFirst/getLast/peekFirst/peekLast ×（空/单元素/多元素）逐格断言（空：remove*/get* NSEE、
  peek*/poll* null；单元素：first==last）。
- **Queue 视图别名证明**：add==addLast、offer==offerLast、element()==getFirst()、peek()==peekFirst()、
  remove()==removeFirst()、poll()==pollFirst()、push==addFirst、pop==removeFirst（同队列前后状态对照）。
- removeFirst/LastOccurrence：三次出现删首/删尾逐状态断言；缺失与 null 参数均返 false 不抛。
- 批量契约：containsAll/removeAll/retainAll/addAll（顺序保持、返回值语义）。
- toArray 契约：保序、够长复用==+null 终止符、精确长度、空 length 0。
- 迭代器 remove 状态机：未 next ISE / 连续两次 ISE / 删当前元素；descendingIterator 支持 remove
  且删的是已访问的尾侧元素。
- fail-fast CME：升序迭代器尾部 add 后 next 抛 CME；降序迭代器头部 add 后 next 抛 CME；
  iterator.remove 是迭代中唯一合法修改。
- null 矩阵：7 个写入口全 NPE；contains(null)/remove(null)/occurrence(null) 返 false。

### 易错规范细节（已锁入断言）

- **JDK-8 fail-fast 方向性**：升序迭代器只在**尾部**结构修改后 CME，降序只在**头部**修改后 CME
  （对端修改不触发——见 ArrayDequeTest 的完整矩阵）。
- 单元素双端队列 getFirst()==getLast()、四查看器同值。
