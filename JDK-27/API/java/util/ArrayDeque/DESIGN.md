# java.util.ArrayDeque 测试方案

对应测试：`ArrayDequeTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 构造器：`ArrayDeque()`、`ArrayDeque(int numElements)`、`ArrayDeque(Collection<? extends E>)`
- Queue 操作：`add(E)` / `offer(E)`、`remove()` / `poll()`、`element()` / `peek()`
- Deque 双端：`addFirst/addLast`、`offerFirst/offerLast`、`removeFirst/removeLast`、
  `pollFirst/pollLast`、`getFirst/getLast`、`peekFirst/peekLast`
- 栈：`push(E)` / `pop()`
- 定向删除：`removeFirstOccurrence(Object)` / `removeLastOccurrence(Object)`
- 查询/批量：`contains` / `size` / `isEmpty` / `clear` / `addAll` / `removeAll` / `retainAll`
- 迭代：`iterator()`、`descendingIterator()`、`spliterator()`、`forEach`

## 2. Java 用户如何使用

```java
ArrayDeque<Integer> queue = new ArrayDeque<>();   // FIFO 队列
queue.offer(1); queue.poll(); queue.peek();

ArrayDeque<Integer> stack = new ArrayDeque<>();   // LIFO 栈（官方推荐）
stack.push(1); stack.pop();

ArrayDeque<Integer> deque = new ArrayDeque<>();   // 双端
deque.addFirst(0); deque.addLast(9); deque.peekFirst(); deque.pollLast();

for (Integer x : deque) { ... }                   // 头到尾
Iterator<Integer> di = deque.descendingIterator(); // 尾到头
```

约束：禁止 null（所有写入口抛 `NullPointerException`；`contains(null)` / `remove(null)` 返回 false）。

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/nio/file/FileTreeWalker.java:61` — `private final ArrayDeque<DirectoryNode> stack = new ArrayDeque<>();`（目录遍历栈）
- `java/util/ArrayDeque.java`（类 javadoc）— 官方说明“用作栈时比 Stack 快、用作队列时比 LinkedList 快”
- `java/io/ObjectOutputStream.java:1249-1351` — 调试信息栈的 `push/pop` 组合（同语义，用 Deque）

结论：ArrayDeque 是 JDK 推荐的无容量限制 Deque，典型用途为栈/队列/双端，配合 `push/pop/offer/poll`。

## 4. 测试用例设计 + 自动化实现

- 正常：Queue 的 offer/poll/peek（FIFO）；Deque 双端 add/offer/peek/poll/get/remove；push/pop LIFO；
  `removeFirstOccurrence/removeLastOccurrence`；`iterator/descendingIterator` 顺序；`toArray` 顺序。
- 组合链路：`addLast/addFirst → peekFirst/peekLast → pollFirst/pollLast → push/pop →
  removeFirstOccurrence`；跨环形边界扩容后 poll 顺序保持不变。
- 边界/异常：空时 `poll/peek` 返回 null、`remove/element/getFirst/getLast/removeFirst/removeLast/pop`
  → `NoSuchElementException`；写入口传 null → `NullPointerException`；`contains(null)/remove(null)`
  返回 false。

健壮性：固定输入、无墙钟/随机；顺序结构无迭代顺序依赖；不依赖 fail-fast 的 CME（ArrayDeque 迭代器
`next()` 不保证抛 CME，已规避）；无全局副作用；headless，无 `@key`。
