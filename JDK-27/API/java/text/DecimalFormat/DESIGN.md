# java.text.DecimalFormat 测试方案

## 1. API 功能（按方法分组）

- 构造器：`DecimalFormat()`、`DecimalFormat(String)`、`DecimalFormat(String,DecimalFormatSymbols)`。
- 格式化/解析：`format(double/StringBuffer/FieldPosition)`、`format(long,...)`、`parse(String,ParsePosition)`、`formatToCharacterIterator`。
- 模式：`toPattern`/`applyPattern`/`toLocalizedPattern`/`applyLocalizedPattern`。
- 前后缀：`get/setPositivePrefix`、`get/setNegativePrefix`、`get/setPositiveSuffix`、`get/setNegativeSuffix`。
- 数值属性：`get/setMultiplier`、`get/setGroupingSize`、`is/setDecimalSeparatorAlwaysShown`、`is/setParseBigDecimal`、`is/setStrict`。
- 符号：`get/setDecimalFormatSymbols`。
- 继承 NumberFormat 的 digits/currency/rounding；契约 `equals`/`hashCode`/`clone`/`toString`。

## 2. Java 用户如何使用

- 构造：`new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))`。
- 典型调用：`df.format(1234567.891)` → `"1,234,567.89"`；`df.parse("1,234.50")` → 数值。
- 组合链路：构造带 pattern → `applyPattern` 切换科学计数/百分比 → `setPositivePrefix`/`setNegativeSuffix` 定制正负显示 → `setMultiplier`/`setGroupingSize`/`setDecimalSeparatorAlwaysShown` 定制 → `setParseBigDecimal(true)` 精确解析 → `setRoundingMode` 控制舍入 → `setDecimalFormatSymbols` 换分隔符。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.NumberFormat` 工厂（`getNumberInstance` 等）内部返回 `DecimalFormat` 实例。
- JDK 测试 `test/jdk/java/text/Format/DecimalFormat/*.java`：大量 pattern/rounding/parse 断言。
- Apache Commons / 报表框架常以 `new DecimalFormat("0.00")` 固定小数位输出金额。
- Spring `NumberStyleFormatter` 等包装 DecimalFormat 提供数字格式化。

## 4. 测试用例设计 + 自动化实现

实现位置：`DecimalFormatTest.java`（`testWhiteBox()` 白盒 + 六个黑盒方法）。

- 正常链路：7 种 pattern（千分位/固定小数/取整/科学计数/百分比/负数子模式/多分组）的正负确定性输出。
- 前后缀链路：`setPositivePrefix`/`setNegativePrefix`/`setNegativeSuffix` 与 getter 回读。
- 数值属性链路：multiplier、grouping size、decimal separator always shown。
- 解析链路：默认 double 解析 vs `setParseBigDecimal(true)` 精确 `BigDecimal`。
- 舍入链路：HALF_EVEN/HALF_UP/DOWN 的精确结果。
- 符号/模式链路：`applyPattern` 换模式、`setDecimalFormatSymbols` 自定义分隔符、`getDecimalFormatSymbols` 回读。
- 契约：`clone` 相等、`hashCode` 相等、`toString` 含 pattern、不同 pattern 不相等。
- 健壮性：固定 Locale.US、无墙钟/随机/全局副作用。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- pattern 语义矩阵 20 pattern × 8 值（分组/科学计数/百分比/千分率/负子模式/引号转义/¤货币/前后缀）
- RoundingMode 全枚举 ×7 模式 ×14 值（HALF_UP 2.5→3、HALF_EVEN 2.5→2/3.5→4、FLOOR/CEILING 负数、UNNECESSARY 精确→ArithmeticException）
- parse 矩阵 20 输入的 value/index/errorIndex 精确断言
- multiplier（format ×100 / parse ÷100）、min/max digits 钳制（00000001/56 截断）、groupingSize
- 符号驱动输出（改 decimalSeparator/groupingSeparator/zeroDigit/minus 后 format 变化）
- 特殊 pattern（空串回退默认、`#,#0` 分组位 0、多小数点）→ IAE
- toPattern/toLocalizedPattern 规范化回读；formatToCharacterIterator run 区间
- 版本锁定串：JDK-8 currency toPattern 无负子模式 + NaN 符号 `\uFFFD`；JDK-27 含负子模式 `(\u00A4#,##0.00)` + NaN 为 "NaN"（CLDR 差异，各自断言实际值）
