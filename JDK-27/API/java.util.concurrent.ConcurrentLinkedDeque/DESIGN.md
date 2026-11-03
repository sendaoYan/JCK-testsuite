# java.util.concurrent.ConcurrentLinkedDeque 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ConcurrentLinkedDeque()`、`(Collection)`。
- 双端入队：`addFirst/addLast`、`offerFirst/offerLast`。
- 双端出队/查看：`pollFirst/pollLast`、`peekFirst/peekLast`、`removeFirst/removeLast`。
- 栈操作：`push(E)`、`pop()`。
- 查询/移除：`size/isEmpty/contains/remove`、`removeFirstOccurrence/removeLastOccurrence`。
- 遍历：`iterator()/descendingIterator()/spliterator()/toArray()/clear()`。
- 实现 `Deque` + `Serializable`，extends `AbstractCollection`。禁止 null 元素。

## 2. Java 用户如何使用
- 无锁双端队列：`ConcurrentLinkedDeque<E> d = new ConcurrentLinkedDeque<>();`。
- 两端操作：`d.offerFirst(e)/d.offerLast(e)`，`d.pollFirst()/d.pollLast()`。
- 栈语义：`d.push(e)`、`e = d.pop()`。
- 按出现位置移除：`d.removeFirstOccurrence(o)`/`d.removeLastOccurrence(o)`。
- 倒序遍历：`d.descendingIterator()`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:104-106`：`ConcurrentLinkedDeque` 是并发双端队列（对应 `ConcurrentLinkedQueue`）。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentQueues/`：覆盖双端 offer/poll/迭代/null 拒绝等契约。
- JDK 源码 `ConcurrentLinkedDeque.java` 内部：基于双向链表 + `VarHandle` CAS 的无锁实现。
- 使用范式：需要并发 + 两端插入/删除的场景（工作窃取、双端缓冲）。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConcurrentLinkedDequeTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractCollection`/implements `Deque`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `addFirst/addLast/offerFirst/offerLast → peekFirst/peekLast → pollFirst/pollLast → removeFirst/removeLast`。
  - 栈：`push×3 → pop×3` LIFO。
  - `removeFirstOccurrence/removeLastOccurrence` 去重。
  - null 拒绝（add/offerFirst/addLast/contains）。
  - `descendingIterator` 顺序。
- 健壮性：固定输入、顺序断言、无全局副作用、headless-safe。
