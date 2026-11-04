# java.util.concurrent.ConcurrentSkipListSet 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ConcurrentSkipListSet()`、`(Comparator)`、`(Collection)`、`(SortedSet)`。
- 导航：`first/last`、`ceiling/floor/higher/lower`、`pollFirst/pollLast`。
- 视图：`subSet/headSet/tailSet`（带 boolean 边界）、`descendingSet/descendingIterator`。
- 操作：`add/remove/contains/size/isEmpty/clear`、`comparator()`、`clone()`。
- 实现 `NavigableSet` + `Cloneable` + `Serializable`，extends `AbstractSet`。禁止 null 元素。

## 2. Java 用户如何使用
- 有序并发集合：`ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();`。
- 范围/最近元素：`s.ceiling(x)`/`s.floor(x)`/`s.higher(x)`/`s.lower(x)`、`s.subSet(lo,true,hi,false)`。
- 倒序遍历：`s.descendingIterator()`。
- 去重：重复 `add` 返回 false。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:182-183`：`ConcurrentSkipListSet` 为 `ConcurrentSkipListMap` 的集合视图式实现。
- JDK 测试 `test/jdk/java/util/Collection/` 与 `test/jdk/java/util/concurrent/ConcurrentQueues/`：覆盖导航方法、子集视图、弱一致等契约。
- 使用范式：需要并发 + 有序去重的场景（有序事件集合、区间查询）。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConcurrentSkipListSetTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractSet`/implements `NavigableSet`/`Cloneable`/`Serializable` + 构造器 + 导航方法形态。
- 黑盒组合链路：
  - `add 乱序 → first/last → 重复 add 拒绝 → ceiling/floor/higher/lower → contains`。
  - `pollFirst/pollLast` 与剩余状态。
  - `comparator()`：自然序 null、反序 `Comparator.reverseOrder()`。
  - `descendingIterator/descendingSet`。
  - `subSet/headSet/tailSet` 边界。
  - null 拒绝（add/contains/remove）。
- 健壮性：固定输入、有序容器天然有序、无全局副作用、headless-safe。

## 4.1 契约深化追加（第二轮，PASS 44 → 127 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | 空态：poll null、first/last NSEE、空迭代器 next NSEE、toString "[]" | testEmptyState |
| P0 | **导航矩阵**（键集 10/20/30/40）：ceiling/floor/higher/lower 全分支含边界 null | testNavigationMatrix |
| P0 | **范围视图**：subSet 两/四参、head/tail 布尔边界；视图活（父 add 可见）；视图写直通；**越界 add IAE（"key out of range"）** | testSubSetsFull |
| P0 | descendingSet 逆序 + 删除直通 + 二次 descending 复原 | testDescendingWriteThrough |
| P0 | 迭代器 remove 支持 + 未 next ISE + 弱一致（并发 add 不抛 CME）+ 升序全等 | testIteratorContract |
| P0 | **null 全拒**：add/contains/remove/ceiling/floor/higher/lower/addAll/ctor 全 NPE | testNullFullRejection |
| P1 | ctor(Collection) 排序去重 + ctor(SortedSet) 保序保比较器 + addAll 计数 + clone 独立 | testConstructorsAndAddAll |
| P1 | 批量 + 跨实现 equals/hashCode 与 TreeSet + toArray 升序/null 终止符 | testBulkAndEqualsTreeSet |
| P1 | spliterator：ORDERED\|SORTED\|DISTINCT\|NONNULL\|CONCURRENT、非 SIZED | testSpliteratorCharacteristics |
| P1 | 序列化：自然序 + 逆序 Comparator round-trip 保序保比较器、suid 锁定 | testSerialization |

### 易错规范细节（实测记录）

- JDK-27 新增 SequencedSet 的 `addFirst`/`addLast` **恒抛 UnsupportedOperationException**（黑盒已断言）。
- null 全拒**含导航查询**：ceiling(null)/floor(null)/higher(null)/lower(null) 都抛 NPE。
- 越界视图 add 的 IAE 消息两侧均为 **"key out of range"**（可断言片段）。
- 迭代器**支持 remove**（与 COW 快照迭代器 UOE 对照）；未 next/连续 remove → IllegalStateException。
- equals 是 Set 语义，与 TreeSet 跨实现相等与迭代顺序无关。
- serialVersionUID = -2479143111061671589L（与 JDK-8 一致）。
