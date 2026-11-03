# java.util.SortedMap — 测试设计案

## 1. API 功能
按键排序的 Map 基接口。继承 SequencedMap。

- 抽象：comparator/subMap/headMap/tailMap/firstKey/lastKey/keySet/values/entrySet。
- default：putFirst/putLast（抛 UOE）、reversed()。

## 2. Java 用户如何使用
`SortedMap<K,V> m = new TreeMap<>();` 区间子图、firstKey/lastKey、视图，遍历保持键序。
链路：put -> firstKey/lastKey -> subMap/headMap/tailMap -> keySet/values/entrySet。

## 3. 开源代码实际用法调研
- OpenJDK `TreeMap`（本地 `~/git/jdk25u-dev`）实现全部抽象方法；putFirst/putLast default 抛
  UnsupportedOperationException（TreeMap 无序插入位置）——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SortedMap/`.

## 4. 测试用例设计 + 自动化实现（SortedMapTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| comparator | 自然序返回 null、反向返回 comparator |
| 区间 | subMap/headMap/tailMap |
| 首末键 | firstKey/lastKey |
| 视图 | keySet/values/entrySet |
| putFirst/putLast | 抛 UOE（default） |
| reversed | 逆序视图 |

共 6 组场景、约 12 个 check（`testBlackBox()`）。
