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
