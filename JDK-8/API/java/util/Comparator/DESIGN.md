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

## 第 2 轮深化（2026-11，契约覆盖，33→186 断言）

新增方法：testSgnContract、testThenComparingChain、testSortStabilityUnderComparator、
testNullsCompositions、testExceptionPropagation、testEqualsContract、
testPrimitiveSpecializationBoundaries。

### 本轮锁定的易错规范细节（先探测后断言，jdk8u372 实测）

- **sgn 契约矩阵**：natural/reversed/reversed().reversed() 三比较器 × {-2,0,2} 全组合反对称 +
  natural/reverse 传递性；`naturalOrder` 与 `reverseOrder` 互为精确反号。
- **3 级链排序**：`comparing(firstChar).thenComparingInt(length).thenComparing(natural)` 把
  `[b2,a2,c1,a1]` 排成 `[a1,a2,b2,c1]`；`chain.reversed()` 整链反号；`reversed()` 不改变原比较器。
- **List.sort 稳定性**：相等键保持相遇顺序（TimSort）。
- **异常同实例透传**：comparing 的 key extractor 抛出的异常**原实例**（`e == boom`）向上传播；
  thenComparing 的二级 comparator 只在主键打平时被调用（主键分出胜负则跳过）。
- **equals 语义**：`comparing(fn)`/`nullsFirst(cmp)` 实例间**身份 equals**（不重写）；
  `naturalOrder()`/`reverseOrder()` 是**缓存单例**（equals 且 ==）；`naturalOrder().reversed().equals(reverseOrder())`。
- **nullsFirst(reverseOrder()) TreeSet** 迭代为 `[null, b, a]`；`nullsFirst(...).reversed()` 连 null 次序一起翻转；
  嵌套 `nullsFirst(nullsLast(cmp))` null 仍最前；`nullsFirst(null,null)==0` **不调用**委托比较器。
- **comparingLong/Int MIN/MAX 边界**；`comparingDouble` 全序：NaN>+Inf、-0.0<+0.0。
- **thenComparing(keyExtractor) 自然键返回 null → NPE**。
- TimSort 的 "Comparison method violates its general contract" IAE **刻意不断言**（实现细节、JDK 版本相关）。

## 注意事项（第 2 轮）

全部固定输入；无随机/墙钟；headless 无 `@key`。
