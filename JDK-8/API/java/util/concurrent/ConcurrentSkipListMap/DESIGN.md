# java.util.concurrent.ConcurrentSkipListMap 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（4）**：`()`、`(Comparator)`、`(Map)`、`(SortedMap)`。
- **有序导航**：firstKey/lastKey/firstEntry/lastEntry/pollFirstEntry/pollLastEntry；
  ceilingKey/floorKey/higherKey/lowerKey 及 Entry 形式（**Entry 为不可变快照**，setValue → UOE）。
- **NavigableMap 视图**：headMap/tailMap/subMap（两参与四参布尔边界），descendingMap/descendingKeySet，
  navigableKeySet；视图**双向联动**且越界写 → `IllegalArgumentException("key out of range")`。
- **ConcurrentMap 原子方法**：putIfAbsent/remove(K,V)/replace 两形式/computeIfAbsent/computeIfPresent/compute/merge。
- **其余**：size/isEmpty/containsKey/containsValue/getOrDefault/forEach(BiConsumer)/putAll/clear/clone。
- 契约：`extends AbstractMap` + `implements ConcurrentNavigableMap, Cloneable, Serializable`；
  **key/value 全拒 null**（NPE）。

## 2. 使用方式 + 开源用法调研

- 可伸缩有序并发 map（跳表）：范围查询（subMap）、按序遍历、并发计数与优先级表。
- JDK javadoc 示例 + OpenJDK 回归 `test/jdk/java/util/concurrent/ConcurrentSkipListMap/`：
  覆盖导航矩阵、视图边界 IAE、null 拒绝、compute/merge。
- 使用范式：`new ConcurrentSkipListMap<>(Comparator.reverseOrder())` 定序；
  `headMap(k).put(...)` 视图写直通；pollFirstEntry 做优先队列。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`ConcurrentSkipListMap`）。
- OpenJDK `test/jdk/java/util/concurrent/ConcurrentSkipListMap/`。
- 双侧实测（8u372-b07 / 27.0.1）：异常与消息表见第 4 部分。

## 4. 测试用例设计（ConcurrentSkipListMapTest.java，PASS 161 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：契约/构造器/导航/视图/批量签名 | testWhiteBox |
| P0 | put/get/firstKey/ceilingKey/remove 基本链（保留原有） | testMap、testNullRejection |
| P0 | **null 全拒矩阵**（13 项：put 两向/get/getOrDefault/containsKey/containsValue/remove 两向/putIfAbsent 两向/merge/computeIfAbsent/computeIfPresent/putAll(null)） | testNullFullRejection |
| P0 | **导航矩阵**（键集 10/20/30/40）：ceiling/floor/higher/lower × Key/Entry × 边界 null | testNavigationMatrix |
| P0 | pollFirst/LastEntry 删除联动 + Entry.setValue UOE + firstEntry 快照性 + 空态 NSEE | testPollEntryContract |
| P0 | **范围视图**：subMap 两/四参布尔边界、head/tail 两形式；视图活（父 put 可见）；视图写直通；**越界 put IAE（消息 "key out of range"）** | testSubMapsFull |
| P0 | descendingMap/keySet 逆序 + 删除直通 + 二次 descending 复原 | testDescendingContract |
| P0 | navigableKeySet/values/entrySet 迭代器 remove 直通；升序全等断言 | testViewsWriteThrough |
| P0 | compute/computeIfAbsent/computeIfPresent/merge 分支矩阵（含调用计数与异常透传） | testComputeMergeBranches |
| P0 | replace 两形式 + remove(K,V) 全分支精确 | testReplacePrecise |
| P1 | 比较器矩阵（自然序 comparator()==null / 逆序首尾）+ ctor(Map/SortedMap)/clone/putAll + ctor(null) NPE | testComparatorAndConstructors |
| P1 | 跨实现 equals/hashCode 与 TreeMap；toString 升序确定全等 | testEqualsTreeMap |
| P1 | forEach(BiConsumer) 键和与升序访问 | testForEachAndSearch |
| P1 | **序列化**：自然序 round-trip；逆序 Comparator round-trip 保序；**非 Serializable Comparator → NotSerializableException** | testSerialization |

### 易错规范细节（实测记录）

- null 全拒含查询端：`containsValue(null)`/`getOrDefault(null,d)`/`remove(null,v)` 也抛 NPE（比 CHM 更严）。
- 视图越界 put 的 IAE 消息两侧均为 **"key out of range"**（headMap/tailMap/subMap 共用），可断言消息片段。
- `pollFirstEntry()`/`firstEntry()` 等返回的 Entry 是**快照**：后续 put 不改变已取出的 Entry；`setValue` 抛 UOE。
- 非序列化比较器（lambda）使整个 map 不可序列化：`writeObject` 抛 `java.io.NotSerializableException`（消息是 lambda 类名，两侧格式不同，只断言类型）。
- entrySet/keySet/values 严格升序（可固定序列断言）；descendingMap 的写通道直通原 map。
- JDK 8 的 CSLM **没有** CHM 那套并行 bulk（forEach(long,..)/search/reduce），只有 Map 默认 `forEach(BiConsumer)`——javap 确认。
- serialVersionUID = -8627078645895051609L（双侧一致）。
