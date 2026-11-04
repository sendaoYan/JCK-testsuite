# java.text.SimpleDateFormat 测试方案

## 1. API 功能（按方法分组）

- 构造器：`SimpleDateFormat()`、`SimpleDateFormat(String)`、`SimpleDateFormat(String,Locale)`、`SimpleDateFormat(String,DateFormatSymbols)`。
- 格式/解析：`format(Date,StringBuffer,FieldPosition)`、`formatToCharacterIterator(Object)`、`parse(String,ParsePosition)`（继承 `parse(String)`）。
- 模式管理：`toPattern()`/`toLocalizedPattern()`/`applyPattern(String)`/`applyLocalizedPattern(String)`。
- 符号/年份：`getDateFormatSymbols()`/`setDateFormatSymbols()`、`get2DigitYearStart()`/`set2DigitYearStart(Date)`。
- 契约：`clone()`、`hashCode()`、`toString()`、`equals()`。

## 2. Java 用户如何使用

- 构造：`new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)`；再 `setTimeZone(TimeZone.getTimeZone("UTC"))` 固定时区。
- 典型调用：`format(new Date())` 输出格式化字符串；`parse("1970-01-01")` 还原 `Date`。
- 组合链路：构造带 pattern → `applyPattern` 换模式 → `format` 确定性输出 → `parse` 往返还原 → `set2DigitYearStart` 控制两位年份世纪 → `setDateFormatSymbols` 自定义月份/星期名 → `formatToCharacterIterator` 遍历字段。
- 模式字母语义：`yyyy`=年、`MM`=月、`dd`=日、`HH`=24 小时、`mm`=分、`ss`=秒、`SSS`=毫秒、`EEE/EEEE`=星期、`MMM/MMMM`=月名、`G`=纪元、`a`=AM/PM、`z`=时区名。

## 3. 开源代码实际用法调研（来源明确）

- JDK 测试 `test/jdk/java/text/Format/SimpleDateFormat/*.java`：大量用例按 pattern 构造并校验 `format`/`parse` 结果。
- JDK 源码 `java.util.logging` 与各日志库使用 `SimpleDateFormat` 生成时间戳。
- Spring/Jackson `ObjectMapper` 时间序列化场景经常使用 `new SimpleDateFormat(pattern)` 定义日期输出格式。
- 通用最佳实践：显式 `Locale` + `TimeZone` 后再 `format`/`parse`（本项目健壮性要求），避免默认时区导致的不确定。

## 4. 测试用例设计 + 自动化实现

实现位置：`SimpleDateFormatTest.java`（`testWhiteBox()` 白盒 + `testPatternLetters()`/`testRoundTrip()`/`testTwoDigitYear()`/`testPatternMutation()`/`testCharacterIteratorAndSymbols()`/`testEqualsCloneHashCode()` 黑盒）。

- 正常链路：17 个模式字母/组合的确定性输出断言（固定 `Locale.US` + `UTC` + `Date(0L)`）。
- 往返链路：`format` → `parse` 还原 `epoch 0`；任意日期解析；垃圾输入抛 `ParseException`。
- 两位年份：`set2DigitYearStart(Date(0L))` 后 `yy` 格式 `70` 且 `parse("70-01-01")` 回 `epoch 0`。
- 模式变更：`applyPattern`/`applyLocalizedPattern` 改模式；`toPattern`/`toLocalizedPattern` 回读；`toString` 含 pattern。
- 符号/迭代器：`setDateFormatSymbols` 自定义月名生效；`formatToCharacterIterator` 产出文本。
- 契约：等 pattern+时区的 `equals`/`hashCode` 相等；`clone` 相等；不同 pattern 不相等。
- 健壮性：固定 Date(0)、locale、timezone；无全局副作用；无文件/网络。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- pattern 字母全表 45 pattern（G/y/M/d/H/h/m/s/S/E/D/F/w/W/a/K/z/Z/'）对固定 Date 的全等输出
- lenient 语义（"2024-02-30" 宽松→3月1日；严格→null + errorIndex 精确 10/19/8）
- 2 位年窗口（2DigitYearStart=2100 时 99→2199 非 2099）
- parse 矩阵（前缀解析/index 前进/errorIndex 精确/尾随文本）
- equals（pattern+locale+tz）/clone/hashCode；symbols 联动（setMonths 后 MMMM 变化）
- formatToCharacterIterator 属性 run（HOUR_OF_DAY0/ERA 等）
- 版本锁定 zone 显示名、默认 pattern（JDK-27 时间含 NNBSP）、localized pattern 字母差异（de: uuuu-MM-tt vs yyyy-MM-dd，applyLocalizedPattern 双侧互反）
