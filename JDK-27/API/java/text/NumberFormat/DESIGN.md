# java.text.NumberFormat 测试方案

## 1. API 功能（按方法分组）

- 常量：`INTEGER_FIELD=0`、`FRACTION_FIELD=1`。
- 工厂：`getInstance`/`getNumberInstance`/`getIntegerInstance`/`getCurrencyInstance`/`getPercentInstance` 各自 0/1 参重载，`getCompactNumberInstance()`/`getCompactNumberInstance(Locale,Style)`，`getAvailableLocales()`。
- 格式化：`format(double)`、`format(long)`、抽象 `format(double,StringBuffer,FieldPosition)`/`format(long,...)`。
- 解析：`parse(String,ParsePosition)`、`parse(String)`（抛 ParseException）、`parseObject`。
- 属性：`isParseIntegerOnly/setParseIntegerOnly`、`isStrict/setStrict`、`isGroupingUsed/setGroupingUsed`、`get/setMaximumIntegerDigits`、`get/setMinimumIntegerDigits`、`get/setMaximumFractionDigits`、`get/setMinimumFractionDigits`、`get/setCurrency`、`get/setRoundingMode`。
- 契约：`equals`、`hashCode`、`clone`。

## 2. Java 用户如何使用

- 工厂实例化：`NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);`。
- 典型调用：`nf.format(1234567.891)` → `"1,234,567.891"`；`nf.parse("1,234.5")` → `1234.5`。
- 组合链路：工厂实例化 → `setGroupingUsed` 控制千分位 → `setMinimumFractionDigits`/`setMaximumFractionDigits` 控制小数位 → `setRoundingMode` 控制舍入 → `format`；解析侧 `setParseIntegerOnly` 截断小数；货币侧 `setCurrency` 切换 `$`/`€`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.DecimalFormat` 继承 NumberFormat，依赖其 min/max digits 与 grouping 字段。
- JDK 测试 `test/jdk/java/text/Format/NumberFormat/*.java`：大量用例构造工厂实例并断言 `format`/`parse` 输出。
- Spring `org.springframework.format.number` 使用 `NumberFormat` 完成数字格式化的类型转换。
- 通用模式：财务/报表代码用 `getCurrencyInstance` + `setCurrency` 输出币种金额。

## 4. 测试用例设计 + 自动化实现

实现位置：`NumberFormatTest.java`（`testWhiteBox()` 白盒 + `testFactoryFormats()`/`testGroupingAndDigits()`/`testParse()`/`testCurrencyAndRounding()`/`testEqualsCloneHashCode()` 黑盒）。

- 正常链路：四种工厂实例的确定性输出（固定 `Locale.US`）。
- 数字分组/小数位/舍入：grouping 开关、最小整数位/固定小数位、最大小数位舍入、HALF_UP 舍入语义。
- 解析链路：`parse` 类型（Long/Double）、整数-only 截断、垃圾输入抛 `ParseException`。
- 货币链路：`setCurrency` USD/EUR 与 `getCurrency` 回读。
- 契约：`clone` 相等、`hashCode` 相等、数字 vs 货币实例不相等。
- 健壮性：固定 Locale.US、无墙钟/随机/全局副作用。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- getInstance/getNumberInstance/getCurrencyInstance/getPercentInstance/getIntegerInstance × Locale 矩阵 + round-trip
- equals/hashCode 跨工厂一致性；format(long)/format(double)/format(BigDecimal) 精度
- digit 钳制（minFrac→1.00000、minInt→00000001、maxInt→56）；integerOnly 解析截断
- parse 错误语义（index 不动 + errorIndex；format(str)→IAE 而非 NPE）
- 常量 INTEGER_FIELD=0/FRACTION_FIELD=1；工厂可用性（getAvailableLocales 含 ENGLISH）
- 版本锁定 de/fr/ja locale 数据全等串（每侧断言自己的实际值）
