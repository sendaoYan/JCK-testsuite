# java.util.concurrent.ConcurrentSkipListMap 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ConcurrentSkipListMap()`、`(Comparator)`、`(Map)`、`(SortedMap)`。
- 导航：`firstKey/lastKey/firstEntry/lastEntry`、`ceilingKey/floorKey/higherKey/lowerKey`（及对应 entry 方法）。
- 视图：`keySet/navigableKeySet/values/entrySet`、`subMap/headMap/tailMap`（带 boolean 边界）、`descendingMap/descendingKeySet`。
- 操作：`put/get/remove/putIfAbsent/replace/computeIfAbsent/compute/merge`、`pollFirstEntry/pollLastEntry`、`clone()`、`comparator()`。
- 实现 `ConcurrentNavigableMap` + `Cloneable` + `Serializable`，extends `AbstractMap`。禁止 null key/value。

## 2. Java 用户如何使用
- 有序并发 map：`ConcurrentSkipListMap<Integer,String> m = new ConcurrentSkipListMap<>();`。
- 范围查询：`m.subMap(lo, true, hi, false)`、`m.headMap(k)`、`m.tailMap(k)`。
- 最近键查询：`m.ceilingKey(x)`/`m.floorKey(x)`/`m.higherKey(x)`/`m.lowerKey(x)`。
- 倒序：`m.descendingMap()`。
- 原子更新默认方法同 `ConcurrentMap`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:182-183,188`：`ConcurrentSkipListMap`/`ConcurrentSkipListSet` 为有序并发容器，且 `ConcurrentSkipListMap` 是 `ConcurrentHashMap` 的有序对应物。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentQueues/` 与 `test/jdk/java/util/Map/`：覆盖导航方法、子视图、弱一致等契约。
- JDK 源码 `ConcurrentSkipListMap.java` 内部：`findNear`/`findPredecessor` 等实现基于跳表（skiplist），保证 `O(log n)` 平均操作。
- 使用范式：需要并发 + 有序键遍历/范围查询的场景（区间索引、有序缓存）。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConcurrentSkipListMapTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractMap`/implements `ConcurrentNavigableMap`/`Cloneable`/`Serializable` + 构造器 + 导航方法形态。
- 黑盒组合链路：
  - 有序导航：`put 乱序 → firstKey/lastKey → ceiling/floor/higher/lower → firstEntry/lastEntry`。
  - 子视图：`subMap(3,true,7,false)/headMap(4)/tailMap(8)` 的边界与大小。
  - 倒序：`descendingMap/descendingKeySet`。
  - `pollFirstEntry/pollLastEntry`。
  - `comparator()`：自然序为 null，`Comparator.reverseOrder()` 反序。
  - null 拒绝；默认方法 `putIfAbsent/compute/merge/replace`。
- 健壮性：固定输入、断言先排序（有序容器天然有序）、无全局副作用、headless-safe。

## 4.1 契约深化追加（第二轮，PASS 66 → 176 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | **null 全拒矩阵**（13 项：put 两向/get/getOrDefault/containsKey/containsValue/remove 两向/putIfAbsent 两向/merge/computeIfAbsent/computeIfPresent/putAll(null)） | testNullFullRejection |
| P0 | **导航矩阵**（键集 10/20/30/40）：ceiling/floor/higher/lower × Key/Entry × 边界 null | testNavigationMatrix |
| P0 | pollFirst/LastEntry 删除联动 + Entry.setValue UOE + firstEntry 快照性（后续 put 不改已取 Entry）+ 空态 NSEE | testPollEntryContract |
| P0 | **范围视图**：subMap 两/四参布尔边界、head/tail 两形式；视图活（父 put 可见）；视图写直通；**越界 put IAE（消息 "key out of range" 两侧一致可断言）** | testSubMapsFull |
| P0 | descendingMap/keySet 逆序 + 删除直通 + 二次 descending 复原 | testDescendingContract |
| P0 | navigableKeySet/values/entrySet 迭代器 remove 直通 + 升序全等 | testViewsWriteThrough |
| P0 | compute/computeIfAbsent/computeIfPresent/merge 分支矩阵（调用计数 + 异常透传） | testComputeMergeBranches |
| P0 | replace 两形式 + remove(K,V) 全分支精确 | testReplacePrecise |
| P1 | 跨实现 equals/hashCode 与 TreeMap + toString 升序确定全等 | testEqualsTreeMapExact |
| P1 | **序列化**：自然序 round-trip；逆序 Comparator round-trip 保序保比较器；**非 Serializable Comparator → NotSerializableException** | testSerialization |

### 易错规范细节（实测记录）

- JDK-27 新增 SequencedMap 的 `putFirst`/`putLast` **恒抛 UnsupportedOperationException**（跳表无头尾插入语义，黑盒已断言）。
- CSLM **没有** CHM 那套并行 bulk（forEach(long,..)/search/reduce），JDK-8 与 JDK-27 均无（javap 核对）；只有 Map 默认 `forEach(BiConsumer)`。
- 视图越界 put 的 IAE 消息两侧均为 **"key out of range"**，可断言消息片段。
- `pollFirstEntry()` 等返回的 Entry 是快照：`setValue` 抛 UOE。
- 非序列化比较器（lambda）使 writeObject 抛 `java.io.NotSerializableException`（消息是 lambda 类名，两侧格式不同，只断言类型）。
- serialVersionUID = -8627078645895051609L（与 JDK-8 一致）。
