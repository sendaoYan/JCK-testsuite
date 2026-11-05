# java.lang.Number 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「取一个 intValue」的冒烟。重点补：白盒结构（抽象类、Object 超类、
> Serializable、四个抽象访问器 + 返回类型）、六大原始转换的截断/宽化、NaN/±Inf 缩窄饱和
> （NaN→0、+Inf→MAX、-Inf→MIN）、byteValue/shortValue 低位截断、以及 float/AtomicInteger/
> AtomicLong/BigInteger/BigDecimal 子类的多态访问。断言确定，headless 安全，无异常路径（访问器不抛）。

## 1. API 提供了哪些功能

- **抽象类**：`Number` 是所有数值包装类型（`Integer/Long/Short/Byte/Float/Double`、`BigInteger/BigDecimal`、
  `AtomicInteger/AtomicLong`）的抽象超类。
- **抽象访问器**：`intValue()`、`longValue()`、`floatValue()`、`doubleValue()`。
- **具体默认**：`byteValue()`（= `(byte) intValue()`）、`shortValue()`（= `(short) intValue()`），
  JLS 5.1.3 缩窄语义。
- **对象**：继承 `Object` 的 `equals/hashCode/toString`（由子类覆写）；实现 `Serializable`。

## 2. Java 用户如何使用

- 不能直接 `new Number()`；通过任一子类实例 `Number n = Integer.valueOf(1)` 或 `new AtomicLong(1)` 获得多态引用。
- 跨类型读原始值 `n.intValue()/longValue()/doubleValue()`；NaN/Infinity 缩窄饱和。
- `Long.valueOf(0x1234567890abcdefL) → intValue` 截断高半；`byteValue/shortValue` 取低位。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Number.html`）：抽象类/四个抽象访问器 + byteValue/shortValue
  默认实现。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Number.java`：
  `byteValue()` 即 `(byte) intValue()`，`shortValue()` 即 `(short) intValue()`。
- **JLS 5.1.3（缩窄原始转换）**：NaN→0，+Inf→MAX，-Inf→MIN 的 int/long 饱和规则。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Number` + `~/git/jdk/test/jdk/java/lang/Number`。

## 4. 测试用例设计（映射到 NumberTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非接口、抽象、Object 超类、Serializable、四个抽象访问器 + 返回类型 | testWhiteBox |
| P0 | 抽象性 + 十种 Number 子类 instanceof | testHierarchy |
| P0 | Integer 六转换（含 byteValue 截断） | testIntegerConversions |
| P0 | Long 窄化截断（intValue/byteValue/shortValue 取低位，longValue 恒等） | testLongNarrowing |
| P0 | Double NaN/±Inf 缩窄饱和 | testDoubleSpecialValues |
| P0 | Float NaN/±Inf 缩窄饱和 | testFloatSpecialValues |
| P1 | AtomicInteger/AtomicLong 缩窄 + 小数向零截断 | testAtomicAndFractional |
| P1 | BigInteger/BigDecimal 六访问器 | testBigIntegerBigDecimal |

## 关键规范细节（易错，已锁入断言）

- **`byteValue() == (byte) intValue()`、`shortValue() == (short) intValue()`**（JLS 5.1.3 缩窄）。
- **NaN 缩窄饱和**：`(int)NaN == 0`、`(long)NaN == 0`、`(byte/short)NaN == 0`。
- **±Inf 缩窄饱和**：`(int)+Inf == Integer.MAX_VALUE`、`(long)+Inf == Long.MAX_VALUE`、
  `(int)-Inf == Integer.MIN_VALUE`、`(long)-Inf == Long.MIN_VALUE`；但 `byteValue()/shortValue()`
  是低位截断（+Inf → -1），不是饱和。
- **四个抽象访问器** `intValue/longValue/floatValue/doubleValue`，返回类型分别为 `int/long/float/double`。
- **Number 无任何方法抛异常**。

## 注意事项

- 结果全确定，无全局副作用；headless 安全，不加 `@key`。
- 避免对 `Float/Double` 做精度依赖的相等断言，NaN/Inf 用 `isNaN` 或饱和值断言。

## 契约深化（2026-11）

- **API 功能补充**：四整型包装 × 六访问器的 42 全表；2^24（float）/2^53（double）精度损失值锁定；
  BigInteger 按位窄化（2^64-1→-1L、2^32→0、2^70→0L）；BigDecimal 向零截断（2.5→2、-2.5→-2）；
  跨包装类型 equals 恒 false 的 6×6 矩阵。
- **使用方式补充**：`-0.0` 位级断言用 `Double.doubleToLongBits`/`Float.floatToIntBits`（== 会混同 ±0）；
  `Long.MAX_VALUE→double` 锁定 `9.223372036854776E18` 与位模式 `0x43E0000000000000L`。
- **参考来源**：实测 Temurin 8u372 与 jdk-27.0.1 双侧（值完全一致）；JLS 5.1.2/5.1.3/IEEE 754；
  OpenJDK Number.java 源码（byteValue/shortValue 即 `(byte)/(short) intValue()`）。
- **新增用例（断言 58→180）**：`testWrapperConversionSuite`（42 全表 + Boolean/Character 非 Number）、
  `testDoubleFloatPrecision`（3.9 截断、257.75→byteValue 1、-0.0 位保真）、`testLongPrecisionLoss`、
  `testSaturationExtremes`（1e300 饱和）、`testStructuralContract`（恰好 6 个公有实例方法名集合）、
  `testBigDecimalBigIntegerExact`、`testCrossTypeEquals`（6×6 + null）。
- **易错细节**：`(byte)+Inf==-1`（低位截断而非饱和到 127）；`2^53+1→double` 舍入到 2^53（round-to-even）；
  `Double(1.0).equals(Float(1.0f))==false`（类型不同）；`Boolean/Character` 不在 Number 家族（需经
  Object 变量做 instanceof，直接写编译不过）。
