# java.util.TreeSet 测试方案

## 1. API 功能（按方法分组）

- 构造器：`TreeSet()`、`TreeSet(Comparator)`、`TreeSet(Collection)`、`TreeSet(SortedSet)`。
- 核心操作：`add`/`remove`/`contains`/`clear`/`addAll`。
- 有序查询：`comparator()`、`first()`/`last()`。
- NavigableSet 上下界：`lower`/`floor`/`ceiling`/`higher`。
- 删除首尾：`pollFirst()`/`pollLast()`。
- 视图：`subSet(from,to)`、`headSet(to)`、`tailSet(from)` 及含/不含边界重载；`descendingSet()`、`descendingIterator()`、`reversed()`。

## 2. Java 用户如何使用

- 构造：`new TreeSet<>()`（自然序）；`new TreeSet<>(Comparator.reverseOrder())`（自定义序）。
- 典型调用：有序去重集合；`first/last`；`ceiling/floor/higher/lower`；区间视图。
- 真实组合链路：
  1. add 乱序 → first/last/迭代有序。
  2. ceiling/floor/higher/lower（缺失键返回最近或 null）。
  3. subSet/headSet/tailSet 视图（含/不含边界），视图 add/remove 联动，越界 add 抛 IAE。
  4. descendingSet/descendingIterator/reversed 倒序 → pollFirst/pollLast。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/Properties/PropertiesStoreTest.java:257`：`new TreeSet<>(props.stringPropertyNames())` —— 对属性名排序。
- JDK 测试 `test/jdk/java/util/Properties/PropertiesStoreTest.java:293`：`new TreeSet<>(comparator)` —— 用自定义比较器逆序。
- JDK 测试 `test/jdk/java/util/Random/EquiDoublesTest.java:181`：`new TreeSet<>()` 收集排序。

## 4. 测试用例设计 + 自动化实现

实现位置：`TreeSetTest.java`（`testWhiteBox()` + `testNaturalOrder()`/`testComparatorOrder()`/`testNavigable()`/`testRangeViewsAndDescending()`/`testNullRejection()`）。

- 正常：自然序/Comparator 序 first/last/迭代。
- 组合：NavigableSet 上下界 + 范围视图联动 + descendingSet/descendingIterator/reversed。
- 边界/异常：add(null) 抛 NPE；subSet(from,to) from>to 抛 IAE；视图越界 add 抛 IAE。
- 健壮性：确定性（固定整数）；无全局副作用；无文件/网络；跨平台。headless-safe。
