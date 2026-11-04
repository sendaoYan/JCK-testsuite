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

## 契约深化（本批新增，两侧同构断言）

- SHORT/LONG 刻度表（1234→1K/1 thousand、1234567→1M/1 million、1200→1K、0、-1234→-1K、999 以下直通、1e10→10B、1500→2K HALF_EVEN）
- parse 矩阵（1.2K→1200、5K→5000 idx2、小写 5k→5 idx1 err-1、bogus null idx0 err0、LONG "1 thousand"→1000 idx10）
- 缺省与位数控制（HALF_EVEN、groupingUsed=false、minInt3→002K、maxFrac1→001.2K、minFrac1→001.0K、CEILING 1200→2K）
- 结构（final、extends NumberFormat、3 参公共构造 + 4 参 pluralRules(String)、无 String[] 第 4 参变体、null pattern→NPE）
- format(String)→IAE；NaN/∞ 走 symbols；formatToCharacterIterator 文本与 format 全等且带 NumberFormat.Field 属性
- 工厂 null locale/style→NPE；clone/equals；自定义 compact 数组（下标=10 的幂，无数字占位则只输出字面量）
