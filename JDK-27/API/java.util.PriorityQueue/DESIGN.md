# java.util.PriorityQueue 测试方案

对应测试：`PriorityQueueTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 构造器：`PriorityQueue()`、`PriorityQueue(int)`、`PriorityQueue(Comparator)`、
  `PriorityQueue(int, Comparator)`、`PriorityQueue(Collection)`、`PriorityQueue(SortedSet)`、
  `PriorityQueue(PriorityQueue)`
- 入队：`add(E)`、`offer(E)`（无界，永不返回 false）
- 出队/查看：`poll()`、`peek()`、`remove()`、`element()`
- 查询/删除：`contains(Object)`、`remove(Object)`、`removeIf(Predicate)`、`removeAll` / `retainAll`
- 其它：`size()` / `isEmpty()` / `clear()`、`comparator()`、`toArray()` / `toArray(T[])`、
  `iterator()`、`spliterator()`、`forEach`

## 2. Java 用户如何使用

```java
PriorityQueue<Integer> q = new PriorityQueue<>();           // 自然序（最小优先）
q.offer(5); q.offer(1); q.peek();                            // 查看最小
while (!q.isEmpty()) { q.poll(); }                           // 依序取最小

PriorityQueue<Integer> max = new PriorityQueue<>(Comparator.reverseOrder()); // 最大优先
max.add(1); max.poll();                                     // 取最大

q.comparator();                                              // 自然序返回 null
q.remove(obj); q.contains(obj);                              // 用 equals
```

约束：禁止 null（`add/offer(null)` 及含 null 集合构造抛 `NullPointerException`）；空队列 `poll/peek`
返回 null、`remove/element` 抛 `NoSuchElementException`。

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码 + test/jdk）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/util/concurrent/DelayQueue.java:104` — `private final PriorityQueue<E> q = new PriorityQueue<E>();`（内部用 PriorityQueue 实现延迟队列）
- `java/util/concurrent/PriorityBlockingQueue.java:903` — `q = new PriorityQueue<E>(Math.max(size, 1), comparator);`（带 Comparator 构造）
- `test/jdk/java/util/PriorityQueue/ForgetMeNot.java:64` — `final PriorityQueue<Integer> q = new PriorityQueue<>();`（官方测试，覆盖“遗忘元素”实现细节）
- `test/jdk/java/util/PriorityQueue/PriorityQueueSort.java` — 官方排序语义测试

结论：PriorityQueue 是“无界优先队列”，被 `DelayQueue`/`PriorityBlockingQueue` 作为底层堆使用，
支持自然序与 Comparator。

## 4. 测试用例设计 + 自动化实现

- 正常：自然序与 Comparator 出队顺序；重复元素全部出队；`comparator()` 访问器；`toArray` 多重集。
- 组合链路：`add/offer → peek → poll → remove/removeIf → contains`；从 `Collection`/`SortedSet` 构造。
- 边界/异常：`add/offer(null)` 及含 null 构造 → `NullPointerException`；空队列 `element/remove` →
  `NoSuchElementException`、`poll/peek` 返回 null。

健壮性：poll 顺序按比较器单调（确定性）；`toArray`/迭代顺序不保证有序，故先排序再断言多重集；
固定输入、无墙钟/随机；无全局副作用；headless，无 `@key`。
