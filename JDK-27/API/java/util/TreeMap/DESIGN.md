# java.util.TreeMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`TreeMap()`、`TreeMap(Comparator<? super K>)`、`TreeMap(Map)`、`TreeMap(SortedMap)`。
- 核心操作：`put`/`get`/`remove`/`clear`/`putIfAbsent`/`compute*`/`merge`（含重写）。
- 有序查询：`comparator()`、`firstKey()`/`lastKey()`、`firstEntry()`/`lastEntry()`。
- NavigableMap 上下界：`lowerKey/lowerEntry`、`floorKey/floorEntry`、`ceilingKey/ceilingEntry`、`higherKey/higherEntry`。
- 删除首尾：`pollFirstEntry()`、`pollLastEntry()`。
- 视图：`subMap(from,to)`、`headMap(to)`、`tailMap(from)` 及含/不含边界重载；`descendingMap()`、`navigableKeySet()`、`descendingKeySet()`。

## 2. Java 用户如何使用

- 构造：`new TreeMap<>()`（自然序）；`new TreeMap<>(Comparator.reverseOrder())`（自定义序）。
- 典型调用：
  - 有序遍历：`firstKey()` → 升序迭代 → `lastKey()`。
  - 最近元素查询：`floorKey(k)`/`ceilingKey(k)`/`lowerKey(k)`/`higherKey(k)`。
  - 区间操作：`subMap(lo, hi)`、`headMap(to)`、`tailMap(from)`。
- 真实组合链路：
  1. 乱序 put → firstKey/lastKey/迭代有序。
  2. ceiling/floor/higher/lower 查询（含缺失键返回最近条目或 null）。
  3. subMap/headMap/tailMap 视图（含/不含边界），视图写入/删除联动原 map，越界写入抛 IAE。
  4. descendingMap 倒序 → pollFirstEntry/pollLastEntry 修改 map。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.Collections.java:2140`：`EmptyNavigableMap() { super(new TreeMap<>()); }` —— 空 NavigableMap 用 TreeMap 作底层。
- JDK 源码 `java.util.Collections.java:3113/3125/3214/3226`：`synchronizedSortedMap(new TreeMap())`、`synchronizedNavigableMap(new TreeMap())` —— 用 TreeMap 提供有序可同步包装。
- JDK 测试 `test/jdk/java/util/TreeMap/HeadTailTypeError.java:40-118`：`headMap(null)`/`tailMap(null)`/`headMap(new Object())` 触发 NPE/ClassCastException 类型错误，验证边界参数校验。

## 4. 测试用例设计 + 自动化实现

实现位置：`TreeMapTest.java`（`testWhiteBox()` + `testNaturalOrder()`/`testComparatorOrder()`/`testNavigable()`/`testRangeViewsAndLinkage()`/`testNullKeyRejection()`）。

- 正常：自然序/Comparator 序的 first/last/迭代顺序。
- 组合：NavigableMap 上下界查询 + 范围视图联动 + descendingMap。
- 边界/异常：null key 抛 NPE（自然序与 Comparator 序）；`subMap(from,to)` from>to 抛 IAE；视图越界 put 抛 IAE；null value 允许。
- 健壮性：确定性（固定整数键）；无全局副作用；无文件/网络；跨平台。headless-safe。
