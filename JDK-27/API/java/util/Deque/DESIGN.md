# java.util.Deque — 测试设计案

## 1. API 功能
双端队列接口：`extends Queue, SequencedCollection`，两端都可插入/删除/检查。

- 头操作：addFirst/offerFirst/removeFirst/pollFirst/getFirst/peekFirst。
- 尾操作：addLast/offerLast/removeLast/pollLast/getLast/peekLast。
- 栈语义：push（=addFirst）/pop（=removeFirst）。
- 对象删除：removeFirstOccurrence/removeLastOccurrence。
- 遍历：descendingIterator（逆序）、reversed()（default 视图）。

## 2. Java 用户如何使用
作队列用 offer/poll；作栈用 push/pop；两端调度用 addFirst/addLast+removeFirst/removeLast。
链路：addFirst/addLast -> getFirst/getLast -> pollFirst/pollLast -> push/pop -> removeFirstOccurrence
-> descendingIterator -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Deque.java`（本地 `~/git/jdk25u-dev`）：
  push/pop 分别委托 addFirst/removeFirst；reversed() 是 default——第 1/3/6 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/ArrayDeque.java` 实现 Deque。
- OpenJDK `test/jdk/java/util/Deque/*`：官方测试覆盖 ArrayDeque/LinkedList 的 Deque 契约。

## 4. 测试用例设计 + 自动化实现（DequeTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 两端增查 | addFirst/addLast/getFirst/getLast/peek/offer |
| 两端删 | removeFirst/removeLast/pollFirst/pollLast；空队列 poll null、remove/get 抛 NSEE |
| push/pop | LIFO |
| 出现删除 | removeFirstOccurrence/removeLastOccurrence |
| descendingIterator | 逆序 |
| reversed | 头尾互换视图 |
| Collection 契约 | contains/size/remove(Object) |

共 7 组场景、约 20 个 check（`testBlackBox()`）。

## 契约深化追加（第二版）

新增 test 方法：testTwelveMethodMatrix / testQueueViewAliasing / testOccurrencesDeep /
testBulkOperations / testToArrayContract / testIteratorRemoveSemantics / testFailFastCme /
testNullMatrix / testAlternatingDrain / testReversedView。

### 覆盖点

- **头尾 12 方法矩阵**（空/单/多元素两态）+ Queue 视图别名证明（add==addLast 等 8 组）+
  occurrence 三次出现删首/删尾、批量契约、toArray 契约、迭代器 remove 状态机、null 矩阵
  ——与 JDK-8 侧同构（详见 JDK-8 侧 DESIGN 对应段落）。
- **JDK-21+ reversed() 活视图**：reversed().addFirst == 原 addLast；reversed().pollFirst 删原尾；
  reversed().reversed() 内容==原序；size 联动；reversed 的 descendingIterator 镜像原顺序；
  新鲜视图上原尾部 add → 视图迭代器 CME（best-effort，新鲜状态实测锁定）。
- **JDK-27 fail-fast 差异**（实测矩阵）：升序迭代器仅尾部 add 触发 CME；降序迭代器也仅尾部 add
  触发；头部 add/removeFirst/removeLast 均不触发——**与 JDK-8 的头部修改触发降序 CME 相反**，
  各侧分别断言（见 ArrayDequeTest.testFailFastMatrix）。

### 易错规范细节

- reversed() 是活视图非快照：透过视图的修改立即反映到原 deque，双向联动。
- best-effort fail-fast：CME 是否触发依赖迭代器栅栏方向，跨版本有差异，按侧实测断言。
