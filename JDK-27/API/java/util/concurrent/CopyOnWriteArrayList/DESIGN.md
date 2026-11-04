# java.util.concurrent.CopyOnWriteArrayList 测试方案

## 1. API 功能（按方法分组）
- 构造器：`CopyOnWriteArrayList()`、`(Collection)`、`(E[])`。
- 列表操作：`add(E)`、`add(int,E)`、`get(int)`、`set(int,E)`、`remove(int)`、`remove(Object)`。
- 查询：`size/isEmpty/contains/indexOf/lastIndexOf`。
- 特有：`addIfAbsent(E)`、`addAllAbsent(Collection)`。
- 批量/默认方法：`forEach/removeIf/replaceAll/sort`。
- 视图：`subList(int,int)`、`iterator()`、`reversed()`。
- 实现 `List` + `RandomAccess` + `Cloneable` + `Serializable`。写时复制（COW）。

## 2. Java 用户如何使用
- 读多写少集合：`CopyOnWriteArrayList<T> l = new CopyOnWriteArrayList<>();`。
- 写操作复制底层数组，读操作无锁。
- 迭代器是快照：创建迭代器后的修改不影响迭代器可见内容（不抛 CME）。
- `l.addIfAbsent(e)` 去重插入；`l.addAllAbsent(c)` 批量去重。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:184-190`：`CopyOnWriteArrayList`/`CopyOnWriteArraySet` 是并发集合，且 COW 列表相比 `synchronized` ArrayList 更适用于读多写少。
- JDK 测试 `test/jdk/java/util/concurrent/CopyOnWriteArrayList/` 与 `CopyOnWriteArraySet/`：覆盖 COW 快照、addIfAbsent、equals 等契约。
- 使用范式：事件监听器列表、配置快照、读远多于写的集合。

## 4. 测试用例设计 + 自动化实现
实现位置：`CopyOnWriteArrayListTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + implements `List`/`RandomAccess`/`Cloneable`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `add → get → set → indexOf/contains → remove(Object)/remove(int) → clear`。
  - `addIfAbsent/addAllAbsent` 去重。
  - 迭代快照：迭代器创建后 `add/set`，迭代器仍见旧快照，列表见新状态。
  - `subList`、`sort/replaceAll`、`equals`。
- 健壮性：固定输入、快照语义断言、无全局副作用、headless-safe。

## 4.1 契约深化追加（第二轮，PASS 60 → 119 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | **null 元素全支持**：add(null)/indexOf(null)/lastIndexOf(null)/contains/set(i,null)/remove(null) | testNullElements、testAddIfAbsentNull |
| P0 | **快照 vs ArrayList CME 对照**（同场景 ArrayList 抛 CME、COWAL 不抛且内容不变）；listIterator 快照 | testIteratorSnapshotVsArrayList |
| P0 | 迭代器 remove/set/add → UOE；listIterator(越界) IOOBE | testIteratorMutatorsUnsupported |
| P0 | 索引边界：get(-1)/remove(99) AIOOBE、add(99,·)/set(5,·)/subList 越界 IOOBE；ctor/addAll(null) NPE | testIndexBounds |
| P0 | ctor(E[]) 防御拷贝独立 + ctor(Collection) 保序 | testConstructors |
| P0 | subList set/add/remove 写通道直通父 | testSubListWriteThrough |
| P1 | sort 稳定性；跨实现 equals/toString 与 ArrayList 全等 | testSortStability、testCrossImplEquals |
| P1 | toArray 快照独立；spliterator ORDERED\|IMMUTABLE\|SIZED\|SUBSIZED、estimateSize 精确 | testToArraySnapshot、testSpliteratorCharacteristics |
| P1 | 序列化 round-trip（含 null 元素）+ suid 锁定 + clone 浅拷贝独立 | testSerialization、testClone |

### 易错规范细节（实测记录）

- JDK-27 新增 SequencedCollection 方法：addFirst/addLast/getFirst/getLast/removeFirst/removeLast 正常工作、`reversed()` 返回逆序视图（黑盒已断言）；**没有 setAll 之类方法**（javap 核对）。
- 快照迭代器三变异操作全 UOE（无写通道）；ArrayList 同场景抛 CME——两种"迭代中修改"行为形成对照断言。
- 越界类型分层：get/remove(int) → `ArrayIndexOutOfBoundsException`；add(int,·)/set/listIterator(i)/subList → `IndexOutOfBoundsException`（消息两侧不同，只断类型）。
- COWAL 允许 null（与 CHM/CSLM 对照）；ctor(E[]) 做防御拷贝。
- serialVersionUID = 8673264195747942595L（与 JDK-8 一致）。
