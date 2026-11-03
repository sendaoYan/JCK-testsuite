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
