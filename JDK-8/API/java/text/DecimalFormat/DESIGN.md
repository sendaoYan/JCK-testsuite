# java.text.DecimalFormat 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/异常、pattern 格式、前后缀、数值属性、解析、舍入、边界、符号、equals/clone、
> 序列化），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`DecimalFormat()`、`DecimalFormat(String)`、`DecimalFormat(String,DecimalFormatSymbols)`。
- 格式/解析：`format(double/StringBuffer/FieldPosition)`、`format(long,...)`、`parse(String,ParsePosition)`、
  `formatToCharacterIterator`。
- 模式：`toPattern`/`applyPattern`/`toLocalizedPattern`/`applyLocalizedPattern`。
- 前后缀：`get/setPositivePrefix`、`get/setNegativePrefix`、`get/setPositiveSuffix`、`get/setNegativeSuffix`。
- 数值属性：`get/setMultiplier`、`get/setGroupingSize`、`is/setDecimalSeparatorAlwaysShown`、
  `is/setParseBigDecimal`。
- 符号：`get/setDecimalFormatSymbols`；继承 NumberFormat 的 digits/currency/rounding。
- 契约：`equals`/`hashCode`/`clone`；`Serializable`。
- JDK-8 无 `isStrict`/`setStrict`（JDK-9+）；`toString()` 继承 `Object.toString()`。

## 2. Java 用户如何使用

- `new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))` 固定符号后确定性输出。
- `setParseBigDecimal(true)` 精确解析；`setRoundingMode` 控制舍入；`setMultiplier`/`setGroupingSize` 定制。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/DecimalFormat.java`。
- JDK-27 同目录 DESIGN.md（差异：JDK-8 的 NaN 符号为 `�`，非 "NaN"；无 isStrict；toString 不含 pattern）。

## 4. 测试用例设计（映射到 DecimalFormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends NumberFormat、3 构造器、toPattern/setParseBigDecimal、Serializable | testWhiteBox |
| P0 | 7 种 pattern 确定性正负输出 | testPatternFormats |
| P0 | 非法 pattern→IAE；ctor(null)→NPE；applyPattern 非法→IAE | testConstructorsAndExceptions |
| P0 | 正负前缀/后缀 | testPrefixSuffix |
| P0 | multiplier/groupingSize/decimalSeparatorAlwaysShown | testMultiplierGroupingSeparator |
| P0 | parseBigDecimal 精确 BigDecimal | testParseBigDecimal |
| P0 | HALF_EVEN/HALF_UP/DOWN 舍入 | testRoundingModes |
| P0 | 边界：0/-0.0/NaN(`�`)/±Infinity/MAX/MIN/空串→null/空 pattern 回退 | testBoundaryValues |
| P0 | 自定义符号 + applyPattern 变更 | testSymbolsAndPatternMutation |
| P0 | digit/currency/grouping/localizedPattern | testDigitSettersAndCurrency |
| P0 | equals/clone/hashCode（toString 非空） | testEqualsCloneHashCode |
| P1 | formatToCharacterIterator + 序列化 round-trip | testCharacterIteratorAndSerialization |

## 关键规范细节（易错，已锁入断言）

- **JDK-8 的 US NaN 符号是 `�`（替换字符），不是 "NaN"**；Infinity 符号是 `∞`。
- 空 pattern `""` 回退到默认 pattern；非法 pattern（如两个小数点）→IAE。
- `parse("")` 返回 null。

## 注意事项

- 固定 Locale.US + DecimalFormatSymbols；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。
