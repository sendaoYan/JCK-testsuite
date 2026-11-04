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
