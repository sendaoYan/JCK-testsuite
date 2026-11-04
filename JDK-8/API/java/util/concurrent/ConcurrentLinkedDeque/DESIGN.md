# java.util.concurrent.ConcurrentLinkedDeque 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`()`、`(Collection)`。
- **Deque 12 方法矩阵（两端×插入/删除/查看）**：`addFirst/addLast`（void）、`offerFirst/offerLast`、
  `pollFirst/pollLast`（空 null）、`peekFirst/peekLast`（空 null）、`getFirst/getLast`（空 NSEE）、
  `removeFirst/removeLast`（空 NSEE）；外加 `push/pop`（栈语义）、`element/remove/peek/poll`（队列语义=首端）。
- **出现位置删除**：`removeFirstOccurrence`/`removeLastOccurrence`（双向各删一个实例）。
- **双向迭代**：`iterator`、`descendingIterator`（均弱一致且**支持 remove**）。
- **Collection 通道**：contains/remove(Object)/addAll/toArray/forEach/removeIf/clear/size。
- 契约：`extends AbstractCollection` + `implements Deque, Serializable`；禁止 null 元素。

## 2. 使用方式 + 开源用法调研

- 无锁双向链表（非阻塞双端队列）：工作窃取调度、栈/队列两用。
- JDK 源码 `package-info.java`：CLD 是并发 Deque 标准实现。
- OpenJDK 回归 `test/jdk/java/util/concurrent/ConcurrentLinkedDeque/`：覆盖两端操作、
  removeFirstOccurrence/removeLastOccurrence、迭代器 remove。
- 使用范式：`ConcurrentLinkedDeque<T> s = new ConcurrentLinkedDeque<>(); s.push(x); s.pop();`（ConcurrentLIFODeque 示例）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`ConcurrentLinkedDeque`）。
- OpenJDK `test/jdk/java/util/concurrent/ConcurrentLinkedDeque/`。
- 双侧实测（8u372-b07 / 27.0.1）。

## 4. 测试用例设计（ConcurrentLinkedDequeTest.java，PASS 105 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends AbstractCollection、12 方法 + push/pop/remove*Occurrence 签名 | testWhiteBox |
| P0 | offerFirst/offerLast/peek/poll 基本往返（保留原有） | testConcurrentLinkedDeque |
| P0 | 空态全矩阵：4 个 poll/peek 系 null + 7 个 remove/get/pop/element 系 NSEE | testEmptyStateMatrix |
| P0 | 非空态 12 方法 + 队列六方法映射（addLast/pollFirst）+ clear | testDequeFullMatrix |
| P0 | push/pop LIFO 栈语义 | testStackSemantics |
| P0 | removeFirst/LastOccurrence 双向精确（[1,2,1,3] 两处 1 可区分）+ [7,0,7,7] 三处方向可区分 | testOccurrenceRemoval |
| P0 | null 矩阵：7 种插入全 NPE + remove*Occurrence(null) NPE + remove(null) NPE + ctor/addAll null NPE；contains(null) false | testNullMatrix |
| P0 | 正/逆迭代器 remove 支持 + 未 next ISE + 弱一致 | testIterators |
| P1 | ctor(Collection)/addAll/toArray/forEach/removeAll/retainAll/removeIf/clear | testCollectionOperations |
| P1 | 序列化 round-trip（两端保持、suid 锁定） | testSerialization |
| P1 | 有界双生产者（头+尾）daemon + latch ≤5s，无丢失无重复 | testConcurrentBothEnds |

### 易错规范细节（实测记录）

- **CLD 特有**：`removeFirstOccurrence(null)`、`removeLastOccurrence(null)`、`remove(null)` 全部抛 NPE
  （`remove(Object)` 委托 `removeFirstOccurrence` → `checkNotNull`），而 `contains(null)` 返回 false —— 与 CLQ（remove(null) 返 false）不同，易混。
- `addFirst/addLast` 是 void（与 `List.add` 返回 boolean 不同）；返回 boolean 的是 offer 系。
- 迭代器（含 descendingIterator）**支持 remove**；未 next 先 remove / 连续两次 remove → `IllegalStateException`。
- `removeFirstOccurrence`/`removeLastOccurrence` 每次只删一个实例，同元素多处时可区分方向。
- serialVersionUID = 876323262645176354L（双侧一致）。
