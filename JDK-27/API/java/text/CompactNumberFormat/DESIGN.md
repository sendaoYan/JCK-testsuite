# java.text.CompactNumberFormat 测试方案

## 1. API 功能（按方法分组）

- 构造器：`CompactNumberFormat(String decimalPattern, DecimalFormatSymbols, String[] compactPatterns)`、`CompactNumberFormat(String, DecimalFormatSymbols, String[], String pluralRules)`。
- 工厂（父类）：`NumberFormat.getCompactNumberInstance(Locale, NumberFormat.Style)`。
- 格式化：`format(long)`、`format(double)`、`format(Object, StringBuffer, FieldPosition)`、`formatToCharacterIterator(Object)`。
- 解析：`parse(String, ParsePosition)`、`parse(String)`、`isParseBigDecimal/setParseBigDecimal`、`isParseIntegerOnly/setParseIntegerOnly`、`isStrict/setStrict`。
- 数字/分组配置：`get/setGroupingSize`、`is/setGroupingUsed`、`get/setMaximum(Integer|Fraction)Digits`、`get/setMinimum(Integer|Fraction)Digits`、`get/setRoundingMode`。
- 工具：`clone()`、`equals`、`hashCode`、`toString`。

## 2. Java 用户如何使用

- 构造：`NumberFormat nf = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);` 或直接 `new CompactNumberFormat("#,##0.0#", DecimalFormatSymbols.getInstance(Locale.US), new String[]{"","","","0K","00K","000K","0M"});`
- 典型调用：`nf.format(1000)` → `"1K"`；`nf.parse("1K")` → `1000`。
- 组合链路：构造 → `format` 紧凑表示 → `setGroupingSize/setGroupingUsed/setMinimumFractionDigits` 调格式 → `setParseBigDecimal(true)` 后 `parse` 返回 `BigDecimal` → `clone` 深拷贝后 `equals/hashCode` 一致。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/CompactNumberFormat.java`：构造函数校验 compactPatterns 与 pluralRules；`setGroupingSize`/`setParseBigDecimal` 等覆盖父类行为。
- JDK 测试 `test/jdk/java/text/Format/CompactNumberFormat/*.java`：`TestClone`、`TestPlurals`、`ToStringTest`、`TestFormatToCharacterIterator`、`TestCNFRounding` 覆盖 clone/复数规则/toString/迭代器/舍入。
- 通用模式：CLDR 紧凑数字（如 `1K`/`1M`）在分析/展示大数时使用。

## 4. 测试用例设计 + 自动化实现

实现位置：`CompactNumberFormatTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：final 类、继承 `NumberFormat`、两个公开构造器、关键方法签名存在。
- 黑盒：直接构造器 format/parse、clone 深拷贝、formatToCharacterIterator、grouping/digit/parse 标志/舍入模式 get-set 组合、toString，并保留工厂实例浅断言。
- 健壮性：固定 `Locale.US`、固定 compactPatterns，headless-safe，不加 `@key`。
