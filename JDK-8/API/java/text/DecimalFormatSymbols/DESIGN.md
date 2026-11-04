# java.text.DecimalFormatSymbols 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造器、Serializable/Cloneable、US 符号、get/set 各 char/String/Currency、
> equals/clone/hashCode），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`DecimalFormatSymbols()`、`DecimalFormatSymbols(Locale)`；工厂 `getInstance()`、
  `getInstance(Locale)`。
- char 符号：`zeroDigit/groupingSeparator/decimalSeparator/perMill/percent/digit/patternSeparator/minusSign/
  monetaryDecimalSeparator` 及其 setter。
- String 符号：`infinity/NaN/currencySymbol/internationalCurrencySymbol/exponentSeparator` 及其 setter。
- Currency：`getCurrency/setCurrency`。
- 契约：`clone/hashCode/equals`；`implements Cloneable, Serializable`。

## 2. Java 用户如何使用

- `DecimalFormatSymbols.getInstance(Locale.US)` 取数字/货币/百分比符号；自定义后交给
  `DecimalFormat.setDecimalFormatSymbols`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/DecimalFormatSymbols.java`。
- JDK-27 同目录 DESIGN.md（差异：JDK-8 无 `getLocale()` 与 `get/setMonetaryGroupingSeparator()`；
  JDK-8 US NaN 符号为 U+FFFD，JDK-27 为 "NaN"）。

## 4. 测试用例设计（映射到 DecimalFormatSymbolsTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：具体类、Serializable/Cloneable、构造器、工厂非空 | testWhiteBox |
| P0 | US 符号（zeroDigit/分隔符/NaN=U+FFFD/infinity=U+221E/货币等） | testUsSymbols |
| P0 | 各 set/get char 与 String 往返 | testMutation |
| P0 | Currency 默认 USD / setCurrency(EUR) 更新国际符号 | testCurrency |
| P0 | equals/clone/hashCode + 变异副本不等 | testEqualsCloneHashCode |
| P0 | ctor(Locale)/no-arg + digit/patternSeparator | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- JDK-8 US `getNaN()` 返回 `�`（非 "NaN"）；`getInfinity()` 返回 `∞`。
- JDK-8 无 `getMonetaryGroupingSeparator`/`getLocale`（JDK-9+ 才有）。

## 注意事项

- 固定 Locale.US；无墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 全字段 get/set round-trip（分隔符/minus/percent/perMill/digits/exponent/Infinity/NaN/货币符/货币代码）
- 符号→DecimalFormat 输出联动实证（改 decimalSeparator 后 format(1.5) 变化）
- equals/hashCode 跨工厂一致；clone 深拷贝独立
- 版本锁定 en 数据 + de/fr/ja 全等串（CLDR 差异记录于差异表）
