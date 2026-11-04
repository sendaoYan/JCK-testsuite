# java.util.Comparator 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。重点补齐 null 矩阵与精确 NPE（naturalOrder 对 null 的 NPE、
> comparing 的 null 函数/自然键返回 null、thenComparing 的 null 重载、nullsFirst/Last(null)）、
> comparingDouble 的 Double.compare 语义（NaN==NaN、+0.0!=-0.0）、边界值、reversed().reversed()。

## 1. API 功能

- **抽象**：`compare(T,T)`、`equals(Object)`。
- **静态工厂/排序**：`naturalOrder()`、`reverseOrder()`、`nullsFirst(Comparator)`、`nullsLast(Comparator)`。
- **键提取**：`comparing(Function)`、`comparing(Function,Comparator)`、`comparingInt/Long/Double`。
- **组合**：`reversed()`、`thenComparing(Comparator)`、`thenComparing(Function)`、
  `thenComparing(Function,Comparator)`、`thenComparingInt/Long/Double`。

## 2. 使用方式

`Comparator.comparing(Person::getName).thenComparingInt(Person::getAge)`；`nullsLast(naturalOrder())`
处理 null；`naturalOrder().reversed()` 降序。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Comparator`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Comparator.java`：
  `comparingDouble` 用 `Double.compare`；`naturalOrder` 对 null 抛 NPE（Comparable 强转）；
  `nullsFirst/Last` 先 `Objects.requireNonNull(comparator)`；键提取函数均 `Objects.requireNonNull`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | naturalOrder/reverseOrder 符号 + MIN/MAX + null NPE + reversed().reversed() | testNaturalAndReverse |
| P0 | comparing 两重载 + null 函数/自然键返回 null NPE + 显式 comparator | testComparing |
| P0 | comparingInt/Long/Double + 边界 + Double.compare NaN/±0.0 + null 函数 NPE | testComparingPrimitives |
| P0 | thenComparing 全重载 + thenComparingInt/Long/Double + null 重载 NPE | testThenComparing |
| P0 | nullsFirst/nullsLast null 矩阵 + null comparator NPE | testNullsFirstLast |

## 关键规范细节

- `naturalOrder().compare(null, x)` → NPE（把 null 强转为 Comparable）。
- `comparing(fn)` 自然键提取返回 null → NPE；`comparing(fn, cmp)` 显式 comparator 可处理 null 键（委托）。
- `comparingDouble` / `thenComparingDouble` 用 `Double.compare`：`NaN==NaN`、`+0.0 != -0.0`。
- `nullsFirst(null)` / `nullsLast(null)` **不抛 NPE**：返回把「所有非 null 值视为相等（0）」的 comparator，null 排序仍生效（先 null 后/先）。
- `thenComparing(null)`（Comparator 重载）与 `thenComparing(null fn)`（Function 重载）→ NPE。

## 注意事项

确定性；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。
