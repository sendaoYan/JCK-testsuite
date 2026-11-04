# java.util.Formatter 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。固定 `Locale.US` 保证浮点/符号输出确定；不依赖文件（除构造器
> null 校验外）、默认 Locale、墙钟。

## 1. API 功能

- **构造器**：无参/Appendable/Locale/Appendable+Locale/String fileName/File/PrintStream/OutputStream
  及 charset 变体。
- **格式化**：`format(String, Object...)`、`format(Locale, String, Object...)`。
- **查询**：`locale()`、`out()`、`toString()`。
- **生命周期**：`flush()`、`close()`、`ioException()`。

## 2. 使用方式

`new Formatter(sb, Locale.US).format("%d %s %.2f", ...)`；`String.format(Locale, fmt, args)`；
`Formatter` 也可直接 `new Formatter()` 并 `toString()` 取结果。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Formatter`（Format string syntax）。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Formatter.java`：
  `Appendable null` → 内部建 StringBuilder（不 NPE）；`PrintStream/OutputStream/String/File null` → NPE；
  `charset null` → NPE；close 后 format/flush → `FormatterClosedException`；close 幂等。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基础 format + String.format | testFormat / testStringFormat |
| P0 | 转换符 d/o/x/X/e/g/a/c/b/h/%%/s | testConversions |
| P0 | 宽度/精度/标志（-/0/+/space/(） | testWidthPrecisionFlags |
| P1 | 参数索引 `%2$s` + 相对索引 `<` | testArgumentIndex |
| P0 | 构造器 null 语义（Appendable null→SB、其余 NPE） | testConstructorNull |
| P1 | locale()/out() | testLocaleAndOut |
| P0 | flush/close/close 幂等/ioException/FCE | testFlushCloseIoException |
| P0 | IllegalFormat 家族（conversion/unknown/missing/flags/null） | testIllegalFormatFamily |
| P0 | 契约深化：转换符矩阵/精度/分组/异常族/getter | 见 Test 内「契约深化」区块 |

## 关键规范细节

- `Formatter((Appendable)null[, Locale])` 合法，内部用 `StringBuilder`（非 NPE）。
- `Formatter((PrintStream/OutputStream/String/File)null)` → NPE；`Formatter(os, null charset)` → NPE。
- `format(null)` → NPE；`%d` 配 String → `IllegalFormatConversionException`；`%q` →
  `UnknownFormatConversionException`；缺参 → `MissingFormatArgumentException`；`-` 与 `0` 冲突 →
  `IllegalFormatFlagsException`。
- close 后 `format`/`flush` → `FormatterClosedException`；close 幂等；`ioException()` 无异常时返回 null。

## 注意事项

固定 `Locale.US`；不硬编码换行（不用 `%n` 断言）；无文件副作用；headless-safe 不加 `@key`。
