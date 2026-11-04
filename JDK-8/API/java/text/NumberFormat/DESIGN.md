# java.text.NumberFormat 测试设计案（JDK-8）

> 覆盖目标：完整契约（工厂、格式、分组/小数位、解析、货币、舍入、equals/clone、子类、parseObject、序列化），
> 非冒烟。

## 1. API 提供了哪些功能

- 常量：`INTEGER_FIELD=0`、`FRACTION_FIELD=1`。
- 工厂：`getInstance`/`getNumberInstance`/`getIntegerInstance`/`getCurrencyInstance`/`getPercentInstance`
  各自 0/1 参重载；`getAvailableLocales()`。
- 格式：`format(double)`、`format(long)`、抽象 `format(double,StringBuffer,FieldPosition)`/
  `format(long,...)`。
- 解析：`parse(String,ParsePosition)`、`parse(String)`（抛 ParseException）、`parseObject`。
- 属性：`isParseIntegerOnly/setParseIntegerOnly`、`isGroupingUsed/setGroupingUsed`、
  `get/setMaximumIntegerDigits`、`get/setMinimumIntegerDigits`、`get/setMaximumFractionDigits`、
  `get/setMinimumFractionDigits`、`get/setCurrency`、`get/setRoundingMode`。
- 契约：`equals`、`hashCode`、`clone`；`Serializable`。
- JDK-8 无 `isStrict`/`setStrict`、无 `getCompactNumberInstance`（JDK-9+/12+）。

## 2. Java 用户如何使用

- 工厂实例化 → `setGroupingUsed`/`setMinimumFractionDigits`/`setMaximumFractionDigits`/`setRoundingMode`
  定制 → `format`；解析侧 `setParseIntegerOnly`；货币侧 `setCurrency`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/NumberFormat.java`。
- JDK-27 同目录 DESIGN.md（差异：JDK-8 的 EUR 货币符号用 `EUR`（`Currency.getSymbol(US)` 返回 "EUR"），
  非 "€"）。

## 4. 测试用例设计（映射到 NumberFormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、extends Format、字段常量、各工厂非空、getAvailableLocales | testWhiteBox |
| P0 | 四种工厂确定性输出（Locale.US）+ getNumberInstance(null)→NPE | testFactoryFormats |
| P0 | grouping 开关 + integer/fraction digit 边界 | testGroupingAndDigits |
| P0 | parse 类型（Long/Double）+ parseIntegerOnly + ParseException + null + 负/前缀/分组 | testParse |
| P0 | currency USD/EUR + rounding HALF_EVEN/HALF_UP | testCurrencyAndRounding |
| P0 | equals/hashCode/clone（number vs currency 不相等） | testEqualsCloneHashCode |
| P0 | 各工厂默认 max fraction（3/2/0/0） | testDefaultMaxFractionDigits |
| P0 | 受保护构造器经子类 + parseObject | testSubclassAndParseObject |
| P1 | 序列化 round-trip | testSerialization |

## 关键规范细节（易错，已锁入断言）

- **JDK-8 的 `setCurrency(EUR)` 后 format 输出 `EUR1,234.50`**（`Currency.getSymbol(US)` 返回 "EUR"），非 "€"。
- `parse("42")`→Long，`parse("3.5")`→Double；默认舍入 HALF_EVEN。
- 受保护构造器只能经子类使用。

## 注意事项

- 固定 Locale.US；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- getInstance/getNumberInstance/getCurrencyInstance/getPercentInstance/getIntegerInstance × Locale 矩阵 + round-trip
- equals/hashCode 跨工厂一致性；format(long)/format(double)/format(BigDecimal) 精度
- digit 钳制（minFrac→1.00000、minInt→00000001、maxInt→56）；integerOnly 解析截断
- parse 错误语义（index 不动 + errorIndex；format(str)→IAE 而非 NPE）
- 常量 INTEGER_FIELD=0/FRACTION_FIELD=1；工厂可用性（getAvailableLocales 含 ENGLISH）
- 版本锁定 de/fr/ja locale 数据全等串（每侧断言自己的实际值）
