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
