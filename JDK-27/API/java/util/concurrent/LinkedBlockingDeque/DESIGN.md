# java.util.concurrent.LinkedBlockingDeque 测试方案

## 1. API 功能（按方法分组）
- 构造器：`LinkedBlockingDeque()`、`(int capacity)`、`(Collection)`。
- 双端非阻塞：`addFirst/addLast`、`offerFirst/offerLast`、`pollFirst/pollLast`、`peekFirst/peekLast`、`removeFirst/removeLast`。
- 双端阻塞：`putFirst/putLast`、`takeFirst/takeLast`、`offerFirst/offerLast(E,long,TimeUnit)`、`pollFirst/pollLast(long,TimeUnit)`。
- 栈：`push(E)`、`pop()`。
- 查询/批量：`size/remainingCapacity/contains`、`removeFirstOccurrence/removeLastOccurrence`、`drainTo`、`descendingIterator`。
- 实现 `BlockingDeque` + `Serializable`，extends `AbstractQueue`。

## 2. Java 用户如何使用
- 双端阻塞队列：`LinkedBlockingDeque<T> d = new LinkedBlockingDeque<>();`。
- 两端插入/取出：`d.putFirst(e)/d.putLast(e)`、`d.takeFirst()/d.takeLast()`。
- 栈语义：`d.push(e)`、`d.pop()`。
- 工作窃取（work stealing）中作为双端任务队列。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:131`：`LinkedBlockingDeque` 类在阻塞队列家族中的说明。
- JDK 测试 `test/jdk/java/util/concurrent/BlockingQueue/` 与 `test/jdk/java/util/Collection/Deque/`：覆盖双端阻塞语义、容量、drainTo 等。
- 使用范式：`ForkJoinPool` 等框架的工作窃取队列、双端任务缓冲。

## 4. 测试用例设计 + 自动化实现
实现位置：`LinkedBlockingDequeTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingDeque`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 双端 `addFirst/addLast/offerFirst/offerLast → peekFirst/peekLast → pollFirst/pollLast → removeFirst/removeLast`。
  - 阻塞 `putFirst/takeFirst` 握手（有界容量 1）。
  - `push/pop` LIFO。
  - `removeFirstOccurrence/removeLastOccurrence`。
  - `drainTo`、`remainingCapacity`、`descendingIterator`、null 拒绝。
- 健壮性：队列自身确定性同步，daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。
