# java.util.Comparator 测试设计

## 1. API 功能
- 核心：`compare(T,T)`。
- 静态工厂：`naturalOrder`、`reverseOrder`、`nullsFirst`、`nullsLast`、`comparing`、`comparingInt/Long/Double`。
- 组合子：`reversed`、`thenComparing`、`thenComparingInt/Long/Double`。
- 便捷：`min`、`max`。

## 2. Java 用户如何使用
- 排序：`list.sort(Comparator.comparing(Person::getName))`。
- 多级排序：`comparingInt(...).thenComparing(...)`。
- 空值排序：`Comparator.nullsFirst(naturalOrder())`。
- 取极值：`natural.max(a, b)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Comparators.java`：包私有 `NullComparator` 支持 `nullsFirst`/`nullsLast`。
- JDK 源码 `java/util/Collections.java`：`sort`/`binarySearch` 依赖 Comparator。
- JDK 自带测试 `test/jdk/java/util/Comparator/BasicTest.java`：工厂与组合子。
- 广泛用法（Spring/Guava）：流排序、TreeMap/TreeSet 自定义比较器。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言接口与关键方法存在。
- 黑盒：
  - naturalOrder/reverseOrder/reversed 符号。
  - comparing/comparingInt/comparing(带显式比较器)/thenComparing。
  - nullsFirst/nullsLast（null 与委托路径）。
  - min/max、thenComparingInt/comparingLong/comparingDouble。
