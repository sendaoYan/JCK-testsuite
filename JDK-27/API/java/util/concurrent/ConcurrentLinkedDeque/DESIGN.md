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

## 4.1 契约深化追加（第二轮，PASS 56 → 102 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | **空态矩阵**：peekFirst/Last、pollFirst/Last、getFirst/Last、removeFirst/Last、remove/pop/element/descendingIterator.next——4 个返回 null 形式 + 7 个 NSEE 形式逐一断言 | testEmptyStateMatrix |
| P0 | **null 矩阵（不对称）**：addFirst/addLast/offerFirst/offerLast/push/add/addAll 全 NPE；contains(null) 返回 false；**removeFirstOccurrence(null)/removeLastOccurrence(null)/remove(null) 抛 NPE**（与 CLQ 的 remove(null) 返回 false 对照） | testNullMatrix |
| P0 | removeFirst/LastOccurrence 三重复元素取首/取尾精确 | testOccurrenceRemovalThree |
| P0 | iterator/descendingIterator 互逆全等 + 迭代器 remove 直通 + 弱一致（迭代中 addLast 不抛 CME） | testIterators |
| P1 | 序列化 round-trip（双端结构保序）+ suid 锁定 | testSerialization |
| P1 | **有界并发双端**：头/尾两个 daemon 生产者各 500（值域不相交防 HashSet 去重假绿），latch ≤5s，1000 元素全可见 | testConcurrentBothEnds |

### 易错规范细节（实测记录）

- CLD 的 `remove(null)`/`removeFirstOccurrence(null)`/`removeLastOccurrence(null)` **抛 NPE**（内部 checkNotNull）而 `contains(null)` 返回 false——非对称契约，CLQ 则全返回 false。
- 空态 7 个 NSEE 形式：getFirst/getLast/removeFirst/removeLast/remove()/pop()/element()（以及空 descendingIterator 的 next）。
- 迭代器支持 remove（活迭代器）；双向迭代器互为逆序。
- JDK-27 的 CLD 额外重写 toString/removeIf/removeAll/retainAll/clear/forEach（javap 核对；黑盒行为断言与 JDK-8 一致）。
- serialVersionUID = 876323262645176354L（双侧一致）。
