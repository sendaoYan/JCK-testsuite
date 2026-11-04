# java.util.SortedMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。comparator（自然序 null / 自定义非 null）、firstKey/lastKey
> 空 NSEE、headMap/tailMap/subMap 范围视图（开/闭）、subMap(from>to) IAE、自然序 null key
> NPE、范围视图活视图。

## 1. API 提供了哪些功能

- **排序元数据**：`comparator`（自然序返回 null）、`firstKey/lastKey`。
- **范围视图**：`headMap(to)`（排他）、`tailMap(from)`（含）、`subMap(from,to)`（[from,to)）。

## 2. Java 用户如何使用

- `sortedMap.subMap(a, b)` 得到 [a,b) 的活视图；`headMap(x)` 取 <x 部分；
  `tailMap(x)` 取 >=x 部分。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/SortedMap.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/TreeMap.java`。

## 4. 测试用例设计（映射到 SortedMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 自然序 + null key NPE | testNaturalOrder |
| P0 | 空 firstKey/lastKey NSEE | testEmpty |
| P0 | 自定义比较器 | testCustomComparator |
| P0 | 范围视图 + from>to IAE | testRangeViews |
| P0 | 活视图 | testBackedViews |

## 关键规范细节（易错，已锁入断言）

- `headMap` 排他、`tailMap` 含、`subMap` [from,to)；`subMap(from>to)` 抛 **IAE**。
- 自然序（comparator==null）下 `put(null,...)` 抛 **NPE**（TreeMap 比较 null key）。
- 范围视图是**活视图**：对其增删映射到底层 Map。

## 注意事项

- 用 TreeMap（自然序，拒 null key）为载体；断言顺序确定。


## 5. 契约级深化追加（2026-11，12→53 断言）

新增方法：testIterationOrderIsKeyOrder / testRangeBoundariesExact / testViewLivenessAndCME /
testEmptyMapRangeViews / testSortedMapEquality。

| 维度 | 锁定内容 |
|---|---|
| 迭代序契约 | keySet/values/entrySet 迭代序与 toString 均严格按键序（"{1=v1, ...}"）|
| 区间边界精确 | headMap(to) 不含 to；tailMap(from) 含 from；subMap [from,to)；firstKey/lastKey/size 逐项精确；subMap(k,k) 空；越界界值（headMap(99)/tailMap(0)）整图/空 |
| comparator 同一性 | comparator() 返回构造传入的同一实例（==）；headMap/tailMap/subMap 视图共享同一 comparator 实例 |
| 视图联动 + CME | 父 put/删反映子图；范围内 put 改值不改 size、范围外 put 对子图不可见；子图迭代器在父结构修改后抛 CME；subMap.clear 只清范围 |
| 空 map 视图 | 空 map 的三个区间视图均空且为活视图（经视图 put 可达父，界值 put IAE）|
| equals | 与 HashMap 同 entrySet 相等；subMap 视图与独立构造同内容 map equals；comparator 方向不影响 equals |

### 易错规范细节（实测锁定）

- **reverse comparator 下 subMap(from,to) 的界值按 comparator 序解释**：`new TreeMap<>(reverseOrder()).subMap(1,2)` 抛 IAE（fromKey>toKey 按 comparator 判定），合法写法是 subMap(2,1)。
- 空 map 上 `subMap(3,3).firstKey()` 仍抛 NSEE（视图空不豁免）。
- 父 map 的**值替换**（put 已有键）不改变子图 size 也不触发子图迭代器 CME（非结构修改）；结构性 put（新键）才触发 CME。
