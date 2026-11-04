# java.text.ChoiceFormat 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/异常、半开区间、format/parse、next/previousDouble、setChoices、边界、
> equals/clone），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`ChoiceFormat(String)`（pattern）、`ChoiceFormat(double[],String[])`（limits + formats）。
- 模式/选择：`applyPattern`、`toPattern`、`setChoices(double[],String[])`、`getLimits()`、`getFormats()`。
- 格式化：`format(long)`、`format(double)`。
- 解析：`parse(String,ParsePosition)`（继承 `parse(String)`）。
- 工具：`nextDouble(double)`、`nextDouble(double,boolean)`、`previousDouble(double)`。
- 契约：`clone`/`hashCode`/`equals`；继承 `NumberFormat`。
- JDK-8 无 `isStrict`/`setStrict`（JDK-9+）；`toString()` 继承 `Object.toString()`。

## 2. Java 用户如何使用

- `new ChoiceFormat("0#none|1#one|2#many")` 按半开区间 `[limit[i], limit[i+1])` 选输出；`parse` 还原下限。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/ChoiceFormat.java`。
- JDK-27 同目录 DESIGN.md（差异：JDK-8 无 isStrict；toString 不含 pattern）。

## 4. 测试用例设计（映射到 ChoiceFormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends NumberFormat、构造器、nextDouble/previousDouble/setChoices | testWhiteBox |
| P0 | pattern 半开区间边界 + format(long) | testPatternRange |
| P0 | null→NPE、长度不匹配→IAE | testConstructorsAndExceptions |
| P0 | parse 还原下限 + 未知项→NaN | testParseRoundTrip |
| P0 | nextDouble/previousDouble 精确相邻 double + 0/MIN/MAX/NaN/inf 边界 | testNextPreviousDouble |
| P0 | setChoices/getLimits/getFormats 数组回读 | testChoices |
| P0 | -inf/+inf/NaN 选择边界 | testBoundaryValues |
| P0 | applyPattern/toPattern + clone/equals/hashCode | testApplyPatternAndContract |

## 关键规范细节（易错，已锁入断言）

- `ChoiceFormat(double[],String[])`/`setChoices` 长度不匹配抛 `IllegalArgumentException`；null 数组抛 NPE。
- `format(NaN)`/`format(-inf)` 选择第一个；`format(+inf)` 选择最后一个。
- `parse("unknown")` 返回 NaN。

## 注意事项

- 无 Locale 依赖（数值区间）；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- double[]/String[] 构造等长校验 + format 边界表（limit 含/不含、'< ' vs '#'）
- nextDouble/previousDouble 全表（含 ±∞/0/MAX/MIN）
- 特殊值 format（-0.0/NaN/-∞ 取第一段）
- parse 往返；applyPattern 往返 + 未排序 limits
- 版本锁定非法 pattern：JDK-8 applyPattern 非法数字 → IllegalArgumentException，JDK-27 → NumberFormatException（差异记录）
