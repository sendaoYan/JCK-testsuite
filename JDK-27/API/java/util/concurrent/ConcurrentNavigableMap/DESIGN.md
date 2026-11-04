# java.util.concurrent.ConcurrentNavigableMap 设计案

## 1. API 功能
线程安全有序 Map 接口（继承 ConcurrentMap + NavigableMap）。支持子区间 `subMap`/`headMap`/`tailMap`、`descendingMap`、有序键视图。

## 2. Java 用户如何使用
并发有序键范围查询：`ceilingKey`/`floorKey` 找最近键、`subMap` 区间遍历。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ConcurrentSkipListMap` 实现；`test/jdk/java/util/concurrent/ConcurrentSkipListMap/` 覆盖。

## 4. 测试用例设计 + 自动化实现（契约级深化）
文件：`ConcurrentNavigableMapTest.java`（白盒 + 黑盒合并，运行时 144 断言；深化前 14）。在原有结构白盒 + 冒烟黑盒之上追加契约维度：

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testWhiteBox（保留+深化） | 原方法面核对保留；追加：双父接口恰为 ConcurrentMap+NavigableMap、9 个视图方法返回类型**收窄**为 ConcurrentNavigableMap/NavigableSet（反射 getReturnType）、类型参数 K/V | P0 |
| 2 | testBlackBox（原冒烟保留） | 端点/导航/subMap/headMap/tailMap/descending 主干 | P0 |
| 3 | testSubMapBoundaries | 4 参布尔边界全矩阵（T/T=2、F/F=0、F/T=1、T/F=1、firstKey/lastKey 随含端漂移）；2 参语义 [lo,hi)、[min,hi)、[min,hi]、[lo,max]、(lo,max]；越全集/空集；空 map 视图 | P0 |
| 4 | testViewWriteLinkage | 视图 put→主 map（size 增）；主 map put 区间内→视图可见/区间外→不可见；双向 remove 联动；视图 get 实时；视图 clear 只清区间 | P0 |
| 5 | testViewRangeEnforcement | subMap/headMap/tailMap 越界 put → IAE **"key out of range"**（实测消息）；边界键随含端放行/拒绝；lo>hi → IAE **"inconsistent range"**；视图 put null → NPE | P0 |
| 6 | testDescendingMap | 逆序迭代、firstKey=max/lastKey=min、entry 互换、put 联动、逆序导航（ceiling/floorKey 反向）、descending∘descending=原序、equals/hashCode 双向、逆序视图 subMap 端点 | P0 |
| 7 | testKeySetViews | navigableKeySet 升序、iterator.remove 写通主 map、keySet() 是 NavigableSet 且 remove 联动、descendingKeySet 逆序、keySet.add → UOE | P1 |
| 8 | testEndpoints | firstKey/lastKey、firstEntry/lastEntry 不删、pollFirst/LastEntry 取出即删、返回 entry setValue → UOE、空 map：entry/poll → null、firstKey/lastKey → NoSuchElementException | P0 |
| 9 | testViewEqualsHashCode | subMap vs HashMap equals 双向 + hashCode 相等；descendingMap 双向；vs TreeMap 跨实现；值变破坏相等 | P1 |
| 10 | testHierarchyAndGenerics | instanceof 五级链、四视图均实现 ConcurrentNavigableMap、各级父类型引用操作、wildcard 升序首键 | P1 |

易错规范细节（实测锁定，JDK-27 与 JDK-8 一致）：
- descendingMap 加新的最小键后 lastKey 变而 firstKey 不变（firstKey 恒为最大键）。
- 视图 equals/hashCode 与方向无关（Map 契约），非快照。
- 越界 put 消息 "key out of range"、lo>hi 消息 "inconsistent range"。
- keySet().add → UOE、poll entry setValue → UOE（CSLM 实现行为）。

优先级：P0 = 边界矩阵 + 双向联动 + 越界拒绝 + descending + 端点；P1 = 键视图/equals/层次。

断言数：144（运行时 `check` 计数）。
