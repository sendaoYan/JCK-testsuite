# java.lang.Float 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseFloat 读一下」的冒烟。重点补：常量（SIZE/BYTES/TYPE、
> MAX_VALUE/MIN_VALUE/MIN_NORMAL 的位模式、正负无穷位模式）、parseFloat（空白 trim、f/d 后缀忽略、
> 十六进制浮点、NaN/Infinity、NFE vs NPE on null）、NaN/-0.0 语义（-0.0 == 0.0 但 compare/equals 区分）、
> 位转换（floatToIntBits/intBitsToFloat/floatToRawIntBits 保留 NaN payload）、compare/compareTo/equals
> （NaN equals NaN、跨类型不等）、hashCode/toString（含 round-trip）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Float`（final，float 包装类，implements `Comparable<Float>`/`Serializable`）。
- **常量**：SIZE/BYTES/TYPE、MAX_VALUE/MIN_VALUE/MIN_NORMAL、POSITIVE_INFINITY/NEGATIVE_INFINITY/NaN。
- **解析**：`parseFloat`/`valueOf`。
- **位转换**：`floatToIntBits`/`intBitsToFloat`/`floatToRawIntBits`。
- **分类**：`isNaN`/`isInfinite`/`isFinite`。
- **比较**：`compare`/`compareTo`/`equals`/`hashCode`/`toString`。

## 2. Java 用户如何使用

- `Float.parseFloat("1.5")` 解析（会 trim 空白、忽略 f/d 后缀、支持十六进制浮点）。
- `floatToIntBits` 把 float 转 IEEE-754 位模式（NaN 规范化为 0x7fc00000，`floatToRawIntBits` 保留 payload）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Float.html`）：parseFloat 语法、位转换、NaN/Infinity 语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Float.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：MIN_NORMAL=0x00800000、NaN 位 0x7fc00000。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Float`。

## 4. 测试用例设计（映射到 FloatTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：SIZE/BYTES/TYPE、MAX/MIN/MIN_NORMAL 位模式、无穷位模式 | testConstants |
| P0 | parseFloat：空白/f,d 后缀/hex/NaN/Infinity/NFE vs NPE(null) | testParse |
| P0 | NaN/Infinity：isNaN/isInfinite/isFinite、NaN!=NaN | testNaNInfinity |
| P0 | 有符号零：==、compare 区分、equals 区分、位模式 | testSignedZero |
| P0 | 位转换：floatToIntBits/intBitsToFloat/floatToRawIntBits（NaN payload） | testBitConversion |
| P0 | compare/compareTo/equals（NaN、跨类型不等） | testCompareEquals |
| P0 | hashCode/toString（round-trip） | testHashCodeToString |

## 关键规范细节（易错，已锁入断言）

- **`Float.MIN_VALUE = 0x0.000002P-126f`（最小正 subnormal）、`MIN_NORMAL = 2^-126`（0x00800000）**。
- **`parseFloat` trim 空白、忽略 f/d 后缀、支持 `0x1.8p1` 十六进制浮点**；`parseFloat(null)` 抛 NPE（非 NFE）。
- **`-0.0f == 0.0f` 但 `Float.compare(-0.0f,0.0f)<0`、`equals` 不等**（位模式不同）。
- **`floatToIntBits(NaN)==0x7fc00000`（规范化）**；`floatToRawIntBits` 保留 NaN payload。
- **`Float.compare` 中 NaN 大于所有值、NaN==NaN**；`Float.equals` 中 NaN equals NaN。
- **`hashCode == floatToIntBits(value)`**；`toString` 与 `parseFloat` round-trip。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale（浮点 toString 用固定算法）。
