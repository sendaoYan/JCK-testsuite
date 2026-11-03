# java.util.NavigableMap — 测试设计案

## 1. API 功能
可导航的有序 Map：按最近键查询、首末条目、区间视图、降序视图。继承 SortedMap。

- 抽象：lowerEntry/lowerKey/floorEntry/floorKey/ceilingEntry/ceilingKey/higherEntry/higherKey、
  firstEntry/lastEntry/pollFirstEntry/pollLastEntry/descendingMap/navigableKeySet/descendingKeySet。
- default：reversed()。

## 2. Java 用户如何使用
`NavigableMap<K,V> m = new TreeMap<>();` lower/floor/ceiling/higher 最近键查询、first/lastEntry、
pollFirst/pollLast 摘取首末、descendingMap 逆序、subMap(from,fi,to,ti) 区间。
链路：put -> 最近键查询 -> 首末/摘取 -> 逆序/区间视图。

## 3. 开源代码实际用法调研
- OpenJDK `TreeMap`（本地 `~/git/jdk25u-dev`）实现全部抽象方法；`NavigableMap` 接口 default reversed()——
  第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/NavigableMap/`.

## 4. 测试用例设计 + 自动化实现（NavigableMapTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 最近键 | lower/floor/ceiling/higher key+entry 形式 |
| 首末 | firstEntry/lastEntry/firstKey/lastKey |
| 摘取 | pollFirstEntry/pollLastEntry 删除并返回 |
| 逆序 | descendingMap/descendingKeySet/navigableKeySet |
| 区间 | subMap/headMap/tailMap 布尔变体 |
| reversed | 逆序视图 |

共 6 组场景、约 20 个 check（`testBlackBox()`）。
