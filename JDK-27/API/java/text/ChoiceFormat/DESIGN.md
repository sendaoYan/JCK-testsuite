# java.text.ChoiceFormat 测试方案

## 1. API 功能（按方法分组）

- 构造器：`ChoiceFormat(String)`（pattern）、`ChoiceFormat(double[],String[])`（limits + formats）。
- 模式/选择：`applyPattern`、`toPattern`、`setChoices(double[],String[])`、`getLimits()`、`getFormats()`。
- 格式化：`format(long)`、`format(double)`。
- 解析：`parse(String,ParsePosition)`（继承 `parse(String)`）。
- 工具：`nextDouble(double)`、`nextDouble(double,boolean)`、`previousDouble(double)`。
- 属性：`isStrict`/`setStrict`；契约 `clone`/`hashCode`/`toString`/`equals`。

## 2. Java 用户如何使用

- 构造：`new ChoiceFormat("0#none|1#one|2#few|5#many")`，或 `new ChoiceFormat(double[]{1,2,3}, String[]{"a","b","c"})`。
- 典型调用：`cf.format(value)` 按半开区间 `[limit[i], limit[i+1])` 选择输出；`cf.parse("one")` 还原下限值。
- 组合链路：构造 pattern → `format` 各边界值 → `parse` 还原 → `setChoices` 换区间 → `nextDouble`/`previousDouble` 定位相邻 double → `clone`/`equals`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.MessageFormat` 内嵌 ChoiceFormat 实现 `{n,choice,...}` 子格式。
- JDK 测试 `test/jdk/java/text/Format/ChoiceFormat/*.java`：边界值 format/parse 断言。
- 通用模式：i18n 复数/分级文本（如 `0#no files|1#one file|2#many files`）。

## 4. 测试用例设计 + 自动化实现

实现位置：`ChoiceFormatTest.java`（`testWhiteBox()` 白盒 + 五个黑盒方法）。

- 正常链路：pattern 区间边界（-1/0/0.5/1/2/4.9/5/100）与 `format(long)` 的确定性输出。
- 解析链路：`parse` 各选择项还原下限值。
- 工具链路：`nextDouble`/`previousDouble`/`nextDouble(d,boolean)` 的精确相邻 double。
- 选择链路：`setChoices`/`getLimits`/`getFormats` 数组回读与半开区间语义。
- 契约：`setStrict`/`isStrict`、`clone`/`equals`/`hashCode`。
- 健壮性：无 Locale 依赖（ChoiceFormat 是数值区间，不依赖默认 locale）、无墙钟/全局副作用。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- double[]/String[] 构造等长校验 + format 边界表（limit 含/不含、'< ' vs '#'）
- nextDouble/previousDouble 全表（含 ±∞/0/MAX/MIN）
- 特殊值 format（-0.0/NaN/-∞ 取第一段）
- parse 往返；applyPattern 往返 + 未排序 limits
- 版本锁定非法 pattern：JDK-8 applyPattern 非法数字 → IllegalArgumentException，JDK-27 → NumberFormatException（差异记录）
