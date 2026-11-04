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


## 5. 契约级深化追加（2026-11，16→78 断言）

新增方法：testIterationOrderIsKeyOrder / testRangeBoundariesExact / testViewLivenessAndCME /
testEmptyMapRangeViews / testSortedMapEquality / testSequencedDefaults（JDK-21+）。

与 JDK-8 侧同源的五个契约块（迭代序/区间边界/comparator 同一性/视图联动 CME/空图视图/equals）
之外，SequencedMap 默认方法锁定：

| 维度 | 锁定内容 |
|---|---|
| putFirst/putLast | SortedMap default **恒抛 UOE**（位置插入与排序不变量冲突）；TreeMap 载体验证；失败后 map 不变 |
| reversed() | 返回活降序视图（DescendingSubMap）：put/remove 双向传导；reversed().reversed() 恢复原序；keySet 降序 |
| firstEntry/lastEntry/pollFirstEntry/pollLastEntry | SequencedMap default 经 firstKey/lastKey 工作；poll 删除首/末；空图全部 null |
