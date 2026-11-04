# java.util.NavigableMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。lower/floor/ceiling/higher 的 key/Entry 两形态（越界 null）、
> firstEntry/lastEntry/pollFirstEntry/pollLastEntry、descendingMap/descendingKeySet/
> navigableKeySet、含 boolean 含/排的范围视图、subMap(from>to) IAE、自然序 null key NPE。

## 1. API 提供了哪些功能

- **最近匹配**：`lower*/floor*/ceiling*/higher*`（key 与 Entry 形态，越界返回 null）。
- **端点/弹出**：`firstEntry/lastEntry/pollFirstEntry/pollLastEntry`。
- **降序视图**：`descendingMap/descendingKeySet`；`navigableKeySet` 升序可导航视图。
- **范围视图**：`subMap/headMap/tailMap` 的 SortedMap 版本 + 含 boolean 的 Navigable 版本。

## 2. Java 用户如何使用

- `navigableMap.floorKey(k)` 找 <=k 的最大键；`pollFirstEntry()` 弹最小项；
  `descendingMap()` 反序遍历；`subMap(2,true,4,true)` 得闭区间 [2,4]。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/NavigableMap.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/TreeMap.java`。

## 4. 测试用例设计（映射到 NavigableMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 最近匹配 key/Entry + null key NPE | testNearest |
| P0 | 端点/弹出 + 空 null | testEntries |
| P0 | 降序视图 | testDescending |
| P0 | 含 boolean 范围视图 + from>to IAE | testRangeViews |

## 关键规范细节（易错，已锁入断言）

- 最近匹配越界返回 **null**（不抛异常）；`firstEntry/lastEntry` 空返回 null（非 NSEE）。
- 含 boolean 的 `subMap/headMap/tailMap` 语义：`subMap(2,true,4,true)` == [2,4]。
- `subMap(from>to)` 抛 **IAE**；自然序 null key 抛 **NPE**。

## 注意事项

- 用 TreeMap（自然序，拒 null key）为载体；断言顺序确定。


## 5. 契约级深化追加（2026-11，23→120 断言）

新增方法：testNearestMatrix / testEntrySnapshotsImmutable / testPollSequence / testDescendingMirror /
testInclusiveBoundsExact / testNavigableKeySetView。

| 维度 | 锁定内容 |
|---|---|
| 四方法全矩阵 | keys {10,20,30} × probes {5,10,15,20,25,30,35}：lower(<)/floor(<=)/ceiling(>=)/higher(>) 28 格精确值（含 null） |
| entry 快照不可变 | firstEntry/lastEntry/pollFirst/pollLast/floor/ceiling/lower/higher Entry 的 setValue 全部 UOE；poll 类快照在抛 UOE 前**已删除** entry |
| poll 序列 | 连续 pollFirstEntry 排干、firstKey 推进；返回当前值；空图 null |
| descending 镜像 | desc.floor(4)=5 / desc.ceiling(4)=3 / desc.lower(4)=5 / desc.higher(4)=3（reversed comparator 下四方法语义翻转）；desc.descendingMap().equals(原)；desc.subMap/headMap/tailMap 界值按降序解释（subMap(3,true,1,true)=[3,1]）；双向写传导；空 desc null |
| inclusive 四重载 | subMap/headMap/tailMap 的 inclusive 布尔 × 存在/不存在界值 firstKey/lastKey/size 精确；越界 put IAE（subMap 上界、排他 fromKey）；inclusive 界内 put 合法 |
| navigableKeySet | 升序迭代、add UOE、remove/pollFirst/pollLast 透传删 entry；descendingKeySet 降序且 remove 透传；descendingKeySet 迭代器父修改后 CME |

### 易错规范细节（实测锁定）

- `pollFirstEntry().setValue(x)` 抛 UOE，但 **entry 已被删除**（poll 先删再返回快照）——断言必须先 expectThrows 再验 size。
- descendingMap 的 floor/ceiling/lower/higher 与直觉相反：floor 变成「原序中 ≥ probe 的最小」。
- headMap(k, **true**).put(k, v) 合法（inclusive 含界值写入）；headMap(k).put(k, v)（排他）抛 IAE。
