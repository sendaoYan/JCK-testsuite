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
