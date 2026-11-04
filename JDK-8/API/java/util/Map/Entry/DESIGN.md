# java.util.Map.Entry 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。getKey/getValue/setValue（返回旧值）、equals/hashCode 契约、
> Java 8 默认方法 comparingByKey/comparingByValue（自然序 + 自定义比较器 + null 参数 NPE）。

## 1. API 提供了哪些功能

- **访问器**：`getKey`、`getValue`、`setValue`（返回旧值）。
- **默认方法（Java 8）**：`comparingByKey()`、`comparingByKey(Comparator)`、
  `comparingByValue()`、`comparingByValue(Comparator)`。

## 2. Java 用户如何使用

- `entry.getKey()/getValue()` 读键值；`entry.setValue(v)` 改值并返回旧值；
  `entries.sort(Map.Entry.comparingByKey())` 按键排序。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Map.Entry.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Map.java`（Entry 默认方法）。

## 4. 测试用例设计（映射到 EntryTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 访问器 + setValue 返回旧值 | testAccessors |
| P0 | equals/hashCode 契约 | testEqualsHashCode |
| P0 | comparingByKey 自然/自定义/null NPE | testComparingByKey |
| P0 | comparingByValue 自然/自定义/null NPE | testComparingByValue |

## 关键规范细节（易错，已锁入断言）

- `comparingByKey(null)`/`comparingByValue(null)` 抛 **NPE**（`Objects.requireNonNull`）。
- 自然序 `comparingByKey()` 遇到 null key 抛 **NPE**（`null.compareTo`）。
- 两 Entry 键值均相等则 equals 为 true 且 hashCode 相等。

## 注意事项

- 用 `AbstractMap.SimpleEntry`（允许 null 键值、实现 equals/hashCode）为载体。
