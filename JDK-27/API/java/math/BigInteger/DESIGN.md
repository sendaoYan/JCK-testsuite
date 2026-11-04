# java.math.BigInteger — 黑盒行为测试设计

## 1. API 功能（按方法分组）
- 构造/工厂：`BigInteger(String)`, `BigInteger(String, int radix)`, `BigInteger(byte[])`（二进制补码大端）, `BigInteger(int signum, byte[] magnitude)`, `valueOf(long)`, `probablePrime(int, Random)`, `nextProbablePrime()`；常量 `ZERO/ONE/TWO/TEN`。
- 算术：`add/subtract/multiply/divide/divideAndRemainder/remainder/mod/pow/sqrt/sqrtAndRemainder/rootn/rootnAndRemainder/gcd/abs/negate/signum`。
- 位运算：`shiftLeft/shiftRight/and/or/xor/not/andNot/testBit/setBit/clearBit/flipBit/getLowestSetBit/bitLength/bitCount`。
- 数论：`modPow/modInverse/isProbablePrime`。
- 比较/哈希：`compareTo/equals/hashCode/min/max`。
- 转换：`toString(int)/toString()/toByteArray` 及 `intValue/longValue/floatValue/doubleValue/*ValueExact`。

## 2. Java 用户如何使用
- 大整数四则：`BigInteger.valueOf(10).add(BigInteger.valueOf(3))`；链式组合 `n.multiply(m).divide(k)`。
- 进制字符串互转：`new BigInteger("ff", 16).toString(2)`。
- 模幂/逆元（RSA/密码学）：`base.modPow(exp, mod)`、`a.modInverse(mod)`。
- 位标志：`BigInteger.ZERO.setBit(i)` 构造位掩码。
- 组合链路：`probablePrime(bitLen, rnd)` → `nextProbablePrime()` → `isProbablePrime(cert)` → `bitLength()`。

## 3. 开源/真实用法调研（本地权威来源）
- JDK 自带测试（`~/git/jdk25u-dev/test/jdk/java/math/BigInteger/`）：
  - `ModPow.java`：验证 `modPow` 大量 `a.modPow(b, m)` 结果一致。
  - `PrimeTest.java` / `ProbablePrime.java`：`probablePrime` + `isProbablePrime` 组合。
  - `ByteArrayConstructorTest.java`：`new BigInteger(byte[])` 与 `toByteArray()` 双补码互转。
  - `CompareToTests.java`：`compareTo` 与 `equals` 一致性。
  - `PrimitiveConversionTests.java` / `TestValueExact.java`：`*ValueExact` 溢出抛 `ArithmeticException`。
- JDK 源码自用：`java/lang/Long.java`、`java/text/DecimalFormat.java`、`java/util/UUID` 使用 `BigInteger` 处理无符号大数。

## 4. 测试用例设计 + 自动化实现
见 `BigIntegerTest.java` 的 `testBlackBox()`，覆盖：
- 构造（十进制/radix/byte[]/signum/valueOf 缓存/固定种子 probablePrime）。
- 算术主链路 + 负数截断语义 + `divideAndRemainder`/`sqrtAndRemainder`/`rootnAndRemainder`。
- 位运算（算术右移、逻辑位操作、位访问器、bitLength/bitCount/getLowestSetBit 边界）。
- 数论（modPow 负指数、modInverse、isProbablePrime 素数/合数）。
- 比较/哈希契约（equal 值 equal 哈希，非 BigInteger 与 null 的 equals）。
- 转换（radix 正/负/零、toByteArray 往返、`*ValueExact` 越界）。
- 异常（除零、mod 非正模、非互素逆元、负 sqrt、溢出 exact、非法 radix/空串/signum 不匹配）。
