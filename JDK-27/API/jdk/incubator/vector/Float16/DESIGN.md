# jdk.incubator.vector.Float16 设计案

## 1. API 功能
IEEE 754 半精度浮点值类（`final class Float16 extends Number implements Comparable<Float16>`）。分组：常量（`NaN/POSITIVE_INFINITY/NEGATIVE_INFINITY/MAX_VALUE/MIN_VALUE/SIZE/PRECISION/BYTES`）、工厂（`valueOf(float/double/int/long/String/BigDecimal)`、`shortBitsToFloat16`）、查询/转换（`floatValue/doubleValue/intValue/shortValue/byteValue/longValue`、`isNaN/isFinite/isInfinite`、`compareTo/compare`）、算术（`add/subtract/multiply/divide/sqrt/fma/negate/abs/max/min`）、位（`float16ToShortBits/float16ToRawShortBits`）。

## 2. Java 用户如何使用
`Float16 h = Float16.valueOf(1.0f); float f = h.floatValue(); boolean finite = Float16.isFinite(h); Float16 sum = Float16.add(h, Float16.valueOf(2.0f));` 链路：valueOf → floatValue/compareTo → add/subtract/multiply/divide → bits 回读。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/Float16.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/BasicFloat16ArithTests.java`（Float16 基础算术与转换测试）。

## 4. 测试用例设计 + 自动化实现
白盒：public/final/extends Number/implements Comparable/常量 SIZE/BYTES/关键方法。黑盒：`valueOf` 各重载、`floatValue/doubleValue/intValue`、`isNaN/isFinite/isInfinite` 常量、`compareTo/compare` 排序、`add/subtract/multiply/divide/max/min/abs/negate` 精确值、`shortBitsToFloat16` 回读、`hashCode` 一致性。
## 5. 契约深化（JDK-27）
- 常量表：MAX 65504/MIN 次正规 5.9604645E-8/MIN_NORMAL 6.1035156E-5/PRECISION 11/EXP ±15/-14。
- toHexString 全格式（0x1.8p0/NaN/±0x0.0p0/0x1.ffcp15/0x0.004p-14）；toString 最短表示（half(0.1f)=="0.1" 但值 0.099975586）；65519/65505 都收敛 65504（非 inf）。
- 排序语义：compare NaN==NaN 且最大、±0 compare 有序但 equals 区分；位模式 1.0=0x3c00、canonical NaN=0x7e00、-0=0x8000、负 NaN 规格化为正。
- 穷举 65536 位型 round-trip：定点 63491（非 NaN 全部 + canonical 0x7e00）、NaN 模式 2046 全 isNaN；valueOf("bogus") NFE 精确消息；byteValue 截断。
