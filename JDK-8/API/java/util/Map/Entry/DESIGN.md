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

## 5. 契约级深化追加（2026-11，14→55 断言）

新增方法：testLiveEntryContract / testSimpleEntryFamily / testEntrySetIterationEntries /
testComparingNullHostility。

| 维度 | 锁定内容 |
|---|---|
| live entry 契约 | entrySet 迭代所得 entry：setValue 返回旧值并反写底层 Map；与独立构造的 SimpleEntry/SimpleImmutableEntry 跨实现 equals 互为对称 |
| hashCode 公式 | key.hashCode() ^ value.hashCode()；"a"=5 → 97^5==100；null key/value 各贡献 0；双 null hash 0 |
| equals 矩阵 | 同键同值 true（跨实现）；异键/异值 false；vs null false；vs 非 Entry 对象 false；自反 true |
| SimpleEntry 家族 | 拷贝构造 equal 且 setValue 后独立；SimpleImmutableEntry.setValue UOE 且值不变；两 wrapper 均容 null 键值；toString 恰为 "k=1" 形式 |
| 快照语义 | 拷贝构造捕获即时值，live entry 后续 setValue 不影响快照 |
| comparing null 敌意 | comparingByValue() 自然序遇 null value NPE；配 nullsFirst/nullsLast 比较器则按 null 排序不抛；comparingByValue 排序对相等值稳定 |

### 易错规范细节（实测锁定）

- live entry（HashMap entrySet 迭代器返回）与 `new SimpleEntry<>(k,v)` **equals 为 true**：Entry.equals 与实现类无关，仅看键值。
- `SimpleImmutableEntry.setValue` 抛 UOE 但 `getValue` 不变——不可变语义完整。
- 自然序 comparingByKey/comparingByValue 对 null 元素抛 NPE 是比较器契约，不是 Entry 契约；换 null 容忍比较器即可排序。
