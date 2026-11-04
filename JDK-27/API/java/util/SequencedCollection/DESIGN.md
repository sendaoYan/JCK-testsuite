# java.util.SequencedCollection — 测试设计案

## 1. API 功能
具有明确迭代顺序的集合接口（JDK 21+）。继承 Collection。

- 抽象：reversed()。
- default：addFirst/addLast/getFirst/getLast/removeFirst/removeLast。

## 2. Java 用户如何使用
`SequencedCollection<E> c = new ArrayList<>();` 在首尾插入/取值/删除、reversed() 逆序视图。
链路：addFirst/addLast -> getFirst/getLast -> removeFirst/removeLast -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `SequencedCollection`（本地 `~/git/jdk25u-dev`）default 首尾方法委托迭代器/removeLast；
  `List`/`Deque` 继承并覆写——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SequencedCollection/`.

## 4. 测试用例设计 + 自动化实现（SequencedCollectionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 首尾插入 | addFirst/addLast 顺序 |
| 首尾取值 | getFirst/getLast |
| 首尾删除 | removeFirst/removeLast 删除并返回 |
| reversed | 逆序视图 |

共 4 组场景、约 10 个 check（`testBlackBox()`）。

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | reversed() 活视图：rev.add 落原表首端（List 有位置语义）、尺寸联动、rev.reversed()==原序 | testReversedLiveView |
| P0 | 空语义矩阵：getFirst/getLast/removeFirst/removeLast → NoSuchElementException | testEmptySemantics |
| P0 | null 元素容忍（ArrayList 载体） | testNullElements |
| P0 | 不可变载体：List.of addFirst/addLast/removeFirst/removeLast → UOE；空 List.of getFirst NSEE 但 removeFirst UOE（不可变先于空）；unmodifiableList 写 → UOE | testImmutableCarriers |
| P0 | Deque 载体：getFirst==element==peek（ArrayDeque/LinkedList） | testDequeCarrier |

### 易错规范细节（实测锁定）

- **List.reversed().add(x) 插入原表首端**（add 等价 view 的 addLast）——与 Set/Map 的无位置 add 相对。
- 不可变性检查**先于**空检查：`List.of().removeFirst()` 抛 **UOE**（不是 NSEE），但 `getFirst()` 可读抛 NSEE。
- 空读类方法（getFirst/getLast）抛 **NSEE**；poll 类（Deque 侧 peek）返回 null。
