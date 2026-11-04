# java.util.concurrent.ConcurrentNavigableMap 设计案

## 1. API 功能
线程安全有序导航 Map 接口（直接父接口恰为 `ConcurrentMap` + `NavigableMap`，泛型 K/V）。核心契约：
- 子区间视图 `subMap(K,boolean,K,boolean)`/`headMap(K,boolean)`/`tailMap(K,boolean)`（及 2 参便捷形式），返回类型**收窄为 ConcurrentNavigableMap**，视图为**活对象**（双向联动 + 越界写拒绝）；
- `descendingMap()` 逆序视图（同样联动、对合：descending 两次还原）；
- `navigableKeySet()`/`keySet()`/`descendingKeySet()` 有序键视图；
- 继承 NavigableMap 的 `firstKey`/`lastKey`/`firstEntry`/`lastEntry`/`pollFirstEntry`/`pollLastEntry` 端点操作。

## 2. Java 用户如何使用 + 开源实际用法调研
- 并发区间查询：时间线/价格档位按 key 区间切片（`subMap(t0,true,t1,true)`）、`ceilingKey`/`floorKey` 找最近档、`descendingMap` 倒序遍历排行榜、`headMap`/`tailMap` 做分页游标。
- 开源用法：`ConcurrentSkipListMap` 是唯一标准实现；Kafka 的时间索引、HBase/Cassandra 的 memtable 参考实现、Lucene 段级并发词典均用「跳表 + 子区间视图」模式。
- OpenJDK `test/jdk/java/util/concurrent/ConcurrentSkipListMap/`（LooseBin/... 一系列）覆盖视图联动与迭代弱一致性。

## 3. 参考来源
- Oracle javadoc `java.util.concurrent.ConcurrentNavigableMap`（视图返回类型收窄、子 Map 的「一致性」说明：视图操作与主 map 相互反映，越界写抛 IllegalArgumentException）。
- OpenJDK `ConcurrentSkipListMap.java` 源码（`key out of range`/`inconsistent range` 消息）。
- `~/git/jdk8u-dev`/`~/git/jdk` 的 `test/jdk/java/util/concurrent/ConcurrentSkipListMap/`。

## 4. 测试用例设计 + 自动化实现
文件：`ConcurrentNavigableMapTest.java`（白盒 + 黑盒合并，运行时 133 断言；基线 6）。

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testConcurrentNavigableMap（原冒烟保留） | firstEntry/lastEntry/ceilingKey/floorKey/subMap 主干 | P0 |
| 2 | testWhiteBox | isInterface、双父接口 ConcurrentMap+NavigableMap、9 个视图方法**返回类型收窄**为 ConcurrentNavigableMap/NavigableSet（反射 getReturnType）、类型参数 K/V | P0 |
| 3 | testSubMapBoundaries | 4 参布尔边界全矩阵（{10,20,30} 上 T/T=2、F/F=0、F/T=1、T/F=1、firstKey/lastKey 随含端漂移）；2 参 subMap=[lo,hi)、headMap=[min,hi)、headMap(T)=[min,hi]、tailMap=[lo,max]、tailMap(F)=(lo,max]、越全集/空集、空 map 上的视图 | P0 |
| 4 | testViewWriteLinkage | 视图 put→主 map 可见（size 增）；主 map put 区间内→视图可见、区间外→视图不可见；双向 remove 联动；视图 get 读实时值；视图 clear 只清区间内 | P0 |
| 5 | testViewRangeEnforcement | subMap 越界 put → IAE **"key out of range"**（实测消息）；headMap/tailMap 越界各一；边界键随含端标志放行/拒绝；lo>hi → IAE **"inconsistent range"**；视图 put null key → NPE | P0 |
| 6 | testDescendingMap | 逆序迭代、firstKey=max/lastKey=min、firstEntry/lastEntry 互换、put 联动主 map、主 map put 可见、逆序导航（ceilingKey/floorKey 方向反转）、descending∘descending=原序、equals/hashCode 双向、逆序视图上的 subMap 端点 | P0 |
| 7 | testKeySetViews | navigableKeySet 升序、iterator.remove 写通主 map、keySet() 返回 NavigableSet 且 remove 联动、descendingKeySet 逆序、keySet.add → UOE | P1 |
| 8 | testEndpoints | firstKey/lastKey、firstEntry/lastEntry 不删除、pollFirstEntry/pollLastEntry 取出即删、返回 entry 不可变（setValue→UOE）、空 map：firstEntry/lastEntry/poll* → null、firstKey/lastKey → NoSuchElementException | P0 |
| 9 | testViewEqualsHashCode | subMap 视图 vs HashMap equals 双向 + hashCode 相等（Map 契约：基于 entrySet 与方向无关）；与 TreeMap 跨实现相等；值变化破坏相等 | P1 |
| 10 | testHierarchyAndGenerics | instanceof 链（→ConcurrentNavigableMap→ConcurrentMap→NavigableMap→SortedMap→Map）、四种视图均实现 ConcurrentNavigableMap、上转型各级引用操作、wildcard 升序迭代首键 | P1 |

易错规范细节（实测锁定）：
- 4 参 subMap 的 firstKey/lastKey 随布尔含端而漂移（F/T 视图 firstKey==hi），别写死。
- descendingMap 的 firstKey 是**最大**键；加新最小键后 lastKey 变、firstKey 不变。
- 视图 equals 与方向无关（descendingMap.equals(m) 双向真、hashCode 相等）——依据 Map 契约（entrySet 求和），不是快照比较。
- 越界 put 的 IAE 消息固定 "key out of range"，lo>hi 构造时抛 "inconsistent range"（CSLM 实现，两侧 JDK 一致）。
- poll 出的 entry setValue → UnsupportedOperationException（不可变快照）。
- keySet().add → UOE 是 ConcurrentSkipListMap 行为（视图不支持加键）。
- null key/value 一律 NPE（CSLM 实现），视图写同样适用。

优先级：P0 = 边界矩阵 + 双向联动 + 越界拒绝 + descending + 端点操作；P1 = 键视图/equals/层次泛型。

断言数：133（运行时 `check` 计数）。
