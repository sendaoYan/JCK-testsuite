# java.text.DecimalFormatSymbols 测试方案

## 1. API 功能（按方法分组）

- 构造器/工厂：`DecimalFormatSymbols()`、`DecimalFormatSymbols(Locale)`、`getInstance()`、`getInstance(Locale)`、`getAvailableLocales()`、`getLocale()`。
- 数字符号：`get/setZeroDigit`、`get/setGroupingSeparator`、`get/setDecimalSeparator`、`get/setMonetaryDecimalSeparator`、`get/setMonetaryGroupingSeparator`、`get/setExponentSeparator`。
- 特殊符号：`get/setPerMill`、`get/setPercent`、`get/setDigit`、`get/setPatternSeparator`、`get/setInfinity`、`get/setNaN`、`get/setMinusSign`。
- 货币符号：`get/setCurrencySymbol`、`get/setInternationalCurrencySymbol`、`get/setCurrency`。
- 契约：`clone`、`equals`、`hashCode`（实现 Cloneable、Serializable）。

## 2. Java 用户如何使用

- 工厂实例化：`DecimalFormatSymbols.getInstance(Locale.US)` 获得该 locale 的数字/货币符号集。
- 典型调用：`dfs.getDecimalSeparator()`、`dfs.getGroupingSeparator()`、`dfs.getCurrencySymbol()`。
- 组合链路：工厂实例化 → 读取全部符号 → `setDecimalSeparator`/`setGroupingSeparator`/`setCurrency` 等自定义 → `DecimalFormat` 通过 `setDecimalFormatSymbols` 消费这些符号 → `clone`/`equals`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.DecimalFormat` 持有 `DecimalFormatSymbols`，用其 decimal/grouping/percent/perMill 符号渲染数字。
- JDK 测试 `test/jdk/java/text/Format/DecimalFormat/DecimalFormatSymbols/*.java`：断言 US locale 的符号集合。
- 通用 i18n：财务/报表框架为本地语言覆盖 `setDecimalSeparator`/`setGroupingSeparator`/`setCurrencySymbol`。

## 4. 测试用例设计 + 自动化实现

实现位置：`DecimalFormatSymbolsTest.java`（`testWhiteBox()` 白盒 + `testUsSymbols()`/`testMutation()`/`testCurrency()`/`testEqualsCloneHashCode()` 黑盒）。

- 正常链路：US locale 全部符号（zeroDigit/grouping/decimal/percent/perMill/minus/infinity/NaN/currencySymbol/intlCurrency/monetaryDecimal/digit/patternSeparator/exponentSeparator/monetaryGrouping）的确定性断言。
- 变更链路：各 `set*` 与 getter 回读（零位/千分位/小数位/百分号/千分号/负号/无穷/NaN/货币符号/货币小数位/指数分隔符）。
- 货币链路：默认 USD、`setCurrency(EUR)` 后 `getCurrency` 与 `getInternationalCurrencySymbol` 联动。
- 契约：两个 US 实例相等、`clone` 相等、`hashCode` 相等、变异后 clone 不相等。
- 健壮性：固定 Locale.US、无墙钟/随机/全局副作用。headless-safe，不加 `@key`。
