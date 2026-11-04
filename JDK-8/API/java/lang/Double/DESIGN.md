# java.lang.Double 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseDouble 读一下」的冒烟。重点补：常量（SIZE/BYTES/TYPE、
> MAX_VALUE/MIN_VALUE/MIN_NORMAL 位模式、正负无穷位模式）、parseDouble（空白 trim、f/d 后缀、
> 十六进制浮点、NaN/Infinity、NFE vs NPE on null）、NaN/-0.0 语义、位转换（doubleToLongBits/
> longBitsToDouble/doubleToRawLongBits 保留 NaN payload）、compare/compareTo/equals（NaN、
> 跨类型不等）、hashCode（XOR 半）/toString（round-trip）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Double`（final，double 包装类，implements `Comparable<Double>`/`Serializable`）。
- **常量**：SIZE/BYTES/TYPE、MAX_VALUE/MIN_VALUE/MIN_NORMAL、POSITIVE_INFINITY/NEGATIVE_INFINITY/NaN。
- **解析**：`parseDouble`/`valueOf`。
- **位转换**：`doubleToLongBits`/`longBitsToDouble`/`doubleToRawLongBits`。
- **分类**：`isNaN`/`isInfinite`/`isFinite`。
- **比较**：`compare`/`compareTo`/`equals`/`hashCode`/`toString`。

## 2. Java 用户如何使用

- `Double.parseDouble("1.5")` 解析（trim 空白、忽略 f/d 后缀、支持十六进制浮点）。
- `doubleToLongBits` 转 IEEE-754 位模式（NaN 规范化为 0x7ff8000000000000，`doubleToRawLongBits` 保留 payload）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Double.html`）：parseDouble 语法、位转换、NaN/Infinity 语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Double.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：MIN_NORMAL=0x0010000000000000、NaN 位 0x7ff8000000000000。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Double`。

## 4. 测试用例设计（映射到 DoubleTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：SIZE/BYTES/TYPE、MAX/MIN/MIN_NORMAL 位模式、无穷位模式 | testConstants |
| P0 | parseDouble：空白/f,d 后缀/hex/NaN/Infinity/NFE vs NPE(null) | testParse |
| P0 | NaN/Infinity：isNaN/isInfinite/isFinite、NaN!=NaN | testNaNInfinity |
| P0 | 有符号零：==、compare 区分、equals 区分、位模式 | testSignedZero |
| P0 | 位转换：doubleToLongBits/longBitsToDouble/doubleToRawLongBits（NaN payload） | testBitConversion |
| P0 | compare/compareTo/equals（NaN、跨类型不等） | testCompareEquals |
| P0 | hashCode（XOR 半）/toString（round-trip） | testHashCodeToString |

## 关键规范细节（易错，已锁入断言）

- **`Double.MIN_VALUE = 0x0.0000000000001P-1022`（最小正 subnormal）、`MIN_NORMAL = 2^-1022`（0x0010000000000000）**。
- **`parseDouble` trim 空白、忽略 f/d 后缀、支持 `0x1.8p1` 十六进制浮点**；`parseDouble(null)` 抛 NPE（非 NFE）。
- **`-0.0 == 0.0` 但 `compare(-0.0,0.0)<0`、`equals` 不等**（位模式不同）。
- **`doubleToLongBits(NaN)==0x7ff8000000000000`（规范化）**；`doubleToRawLongBits` 保留 NaN payload。
- **`hashCode == (int)(bits ^ (bits >>> 32))`**；`toString` 与 `parseDouble` round-trip。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale。
