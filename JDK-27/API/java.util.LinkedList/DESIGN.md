# java.util.LinkedList 测试方案

对应测试：`LinkedListTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 构造器：`LinkedList()`、`LinkedList(Collection<? extends E>)`
- List 操作：`add(E)` / `add(int, E)` / `addAll`、`get(int)` / `set(int, E)` / `remove(int)` /
  `remove(Object)`、`indexOf` / `lastIndexOf` / `contains` / `size` / `clear`
- Queue 操作：`offer(E)` / `poll()` / `peek()`、`add(E)` / `remove()` / `element()`
- Deque 双端：`addFirst/addLast`、`offerFirst/offerLast`、`removeFirst/removeLast`、
  `pollFirst/pollLast`、`getFirst/getLast`、`peekFirst/peekLast`
- 定向删除：`removeFirstOccurrence(Object)` / `removeLastOccurrence(Object)`
- 栈：`push(E)` / `pop()`
- 迭代：`iterator()`、`listIterator(int)`、`descendingIterator()`、`spliterator()`

## 2. Java 用户如何使用

```java
LinkedList<String> q = new LinkedList<>();      // 当队列
q.offer("a"); q.poll(); q.peek();               // FIFO

LinkedList<String> stack = new LinkedList<>();  // 当栈
stack.push("a"); stack.pop();                   // LIFO

LinkedList<String> d = new LinkedList<>();      // 当双端队列
d.addFirst("a"); d.addLast("z"); d.pollFirst(); d.pollLast();

for (String s : d) { ... }                      // 顺序迭代
Iterator<String> di = d.descendingIterator();   // 反向迭代
```

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/security/Security.java:709` — `new LinkedList<>(Arrays.asList(allProviders))`（从数组集合构造）
- `jdk/internal/foreign/abi/BindingInterpreter.java:36` — `Deque<Object> stack = new LinkedList<>();`（作 null 友好的 Deque 栈）
- `javax/security/auth/Subject.java:948` — `LinkedList<E> output = new LinkedList<>();`（结果收集）
- `sun/net/www/protocol/http/AuthCacheImpl.java:59` — `list = new LinkedList<AuthCacheValue>();`（链表存储）
- `java/util/LinkedList.java:59`（javadoc）— 官方示例 `Collections.synchronizedList(new LinkedList(...))`

结论：LinkedList 常作“队列 / 栈 / 双向链表”，其允许 null 的特性被 `BindingInterpreter` 明确利用。

## 4. 测试用例设计 + 自动化实现

- 正常：List 增删查改、`indexOf/lastIndexOf/contains`；Deque 双端 add/offer/peek/poll/get/remove。
- 组合链路：`addAll → get/set/remove → indexOf/lastIndexOf → removeAll/retainAll`；
  `offer/poll/peek` 队列消费；`push/pop` LIFO；`descendingIterator` 反向；`listIterator` add/set/remove；
  `removeFirstOccurrence/removeLastOccurrence`。
- 边界/异常：空队列 `poll/peek` 返回 null、`remove/element` → `NoSuchElementException`；
  空表 `getFirst/getLast/removeFirst/removeLast/pop` → `NoSuchElementException`；
  结构修改后 `iterator/listIterator/descendingIterator` → `ConcurrentModificationException`；
  允许 null 元素。

健壮性：固定输入、无墙钟/随机；断言顺序结构（LinkedList 顺序确定）；无全局副作用；headless，无 `@key`。
