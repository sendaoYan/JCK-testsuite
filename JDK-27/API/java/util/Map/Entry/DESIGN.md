# java.util.Map.Entry — 测试设计案（JDK-27）

> 覆盖目标：**完整契约**。getKey/getValue/setValue、equals/hashCode 公式、SimpleEntry/
> SimpleImmutableEntry 家族语义、comparingByKey/ByValue（含 null 容忍比较器组合），以及
> JDK-9+ 静态工厂 `Map.entry(k,v)` 的不可变/null 敌意断言。

## 1. API 功能

- 访问器：`getKey`/`getValue`/`setValue`（返回旧值）。
- 默认方法：`comparingByKey()`、`comparingByKey(Comparator)`、`comparingByValue()`、
  `comparingByValue(Comparator)`。
- 承载实现：`AbstractMap.SimpleEntry`、`SimpleImmutableEntry`、Map 实现的 entrySet live entry、
  `Map.entry(k,v)`（JDK-9+，不可变、null 敌意）。

## 2. Java 用户如何使用

- entrySet 迭代修改值；entries.sort(Map.Entry.comparingByKey())；Map.entry 构造不可变 entry
  用于 Map.ofEntries。

## 3. 参考来源

- Oracle javadoc java.util.Map.Entry（JDK-27）。
- OpenJDK `~/git/jdk` java.base/java/util/Map.java。

## 4. 测试用例设计（EntryTest.java）

| 场景 | 方法 |
|---|---|
| 访问器/equals/hashCode/comparing | testAccessors/testEqualsHashCode/testComparingByKey/testComparingByValue（原有保留） |
| live entry + 跨实现 equals + hash 公式 | testLiveEntryContract |
| SimpleEntry/SimpleImmutableEntry 家族 | testSimpleEntryFamily |
| entrySet 迭代快照 vs live | testEntrySetIterationEntries |
| comparing null 敌意与容忍 | testComparingNullHostility |
| JDK-9+ Map.entry 工厂 | testMapEntryFactory（setValue UOE、null NPE、与 SimpleEntry 互等、ofEntries 可用） |

## 关键规范细节

- entry.hashCode = key.hashCode() ^ value.hashCode()；null 贡献 0。
- live entry 与 SimpleEntry 同内容 equals true（Entry.equals 只看键值）。
- Map.entry(null,·)/(·,null) 抛 NPE；setValue UOE；toString "k=1"。
