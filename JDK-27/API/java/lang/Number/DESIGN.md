# java.lang.Number 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **抽象类**：`Number` 是所有数值包装类型（`Integer/Long/Short/Byte/Float/Double`、`BigInteger/BigDecimal`、`AtomicInteger/AtomicLong`）的抽象超类。
- **抽象访问器**：`intValue()`、`longValue()`、`floatValue()`、`doubleValue()`。
- **具体默认**：`byteValue()`（= `(byte) intValue()`）、`shortValue()`（= `(short) intValue()`），JLS 5.1.3 缩窄语义。
- **对象**：继承 `Object.equals/hashCode/toString`（由子类覆写）。

## 2. Java 用户如何使用

- **构造/工厂**：不能直接 `new Number()`；通过任一子类实例 `Number n = Integer.valueOf(1)` 或 `new AtomicLong(1)` 获得多态引用。
- **典型调用**：跨类型读原始值 `n.intValue()/longValue()/doubleValue()`；NaN/Infinity 缩窄饱和 `(int)NaN==0`、`(int)+Inf==Integer.MAX_VALUE`、`(int)-Inf==Integer.MIN_VALUE`。
- **真实组合链路**：`Long.valueOf(0x1234567890abcdefL) → intValue 截断高半 → byteValue 取低字节`；`Double.valueOf(NaN/±Inf) → intValue/longValue/byteValue/shortValue` 饱和。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `java.lang.Integer/Long/Float/Double` 均 `extends Number` 并覆写六个访问器（缩窄与宽化语义在此定义）。
  - `java.util.concurrent.atomic.AtomicInteger/AtomicLong` 亦 `extends Number`，使并发计数器可当作 `Number` 统一读取。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Number/`、`test/jdk/java/lang/Integer/` 覆盖缩窄与 NaN/Infinity 饱和。
- **Guava**（`com.google.common.math`、`com.google.common.primitives`）：大量 API 接受/返回 `Number` 或 `Number` 子类做宽化/缩窄（如 `Ints.saturatedCast(long)` 等价于 JLS 5.1.3 饱和）。
- **Spring Framework**（`NumberUtils.convertNumberToTargetClass`）：把 `Number` 从一种具体子类转换到目标子类，正是 `Number` 多态访问器的典型消费方。

## 4. 测试用例设计 + 自动化实现（→ NumberTest.java）

- **正常**：`Number` 抽象性 + `Object` 超类；`Integer/Long/Short/Byte/Float/Double/BigInteger/BigDecimal/AtomicInteger/AtomicLong` 都是 `Number`。
- **组合**：`Integer(300) → intValue/longValue/floatValue/doubleValue/byteValue/shortValue`；`Long(0x1234567890abcdef) → intValue 截断高半`；`AtomicInteger/AtomicLong` 缩窄；`Double(3.99/-3.99) → intValue` 向零截断。
- **边界**：`Double` NaN/±Inf 缩窄饱和（NaN→0、+Inf→MAX、-Inf→MIN）；`Float` NaN/±Inf 同样饱和。
- **异常**：无（Number 访问器不抛异常）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；截断/饱和结果确定；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

## 契约深化（2026-11）

- **API 功能补充**：四整型包装 × 六访问器的 42 全表；2^24/2^53 精度损失值锁定；
  BigInteger 按位窄化（2^64-1→-1L、2^32→0、2^70→0L）；BigDecimal 向零截断（2.5→2、-2.5→-2）；
  跨包装类型 equals 恒 false 的 6×6 矩阵；公有实例方法名恰为 6 个 xxxValue 的集合断言。
- **使用方式补充**：`-0.0` 用 `Double.doubleToLongBits`/`Float.floatToIntBits` 位级断言（== 混同 ±0）；
  `Long.MAX_VALUE→double` 锁定 `9.223372036854776E18` 与位模式 `0x43E0000000000000L`。
- **参考来源**：实测 jdk-27.0.1.0.2 与 Temurin 8u372 双侧（值完全一致）；JLS 5.1.2/5.1.3。
- **新增用例（断言 58→180）**：`testWrapperConversionSuite`、`testDoubleFloatPrecision`
  （3.9 截断、257.75→byteValue 1、-0.0 位保真）、`testLongPrecisionLoss`、`testSaturationExtremes`、
  `testStructuralContract`、`testBigDecimalBigIntegerExact`、`testCrossTypeEquals`（6×6 + null）。
- **易错细节**：`(byte)+Inf==-1`（低位截断而非饱和）；`2^53+1→double` round-to-even 到 2^53；
  `Double(1.0).equals(Float(1.0f))==false`；`Boolean/Character` 非 Number（经 Object 变量断言，
  直接 instanceof 编译不过）。
