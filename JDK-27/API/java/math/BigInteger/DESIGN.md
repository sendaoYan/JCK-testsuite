# java.math.BigInteger — 黑盒行为测试设计

## 1. API 功能（按方法分组）
- 构造/工厂：`BigInteger(String)`, `BigInteger(String, int radix)`, `BigInteger(byte[])`（二进制补码大端）, `BigInteger(int signum, byte[] magnitude)`, `valueOf(long)`, `probablePrime(int, Random)`, `nextProbablePrime()`；常量 `ZERO/ONE/TWO/TEN`（TWO 自 JDK-9 公开）。
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
- JDK 自带测试（`~/git/jdk25u-dev/test/jdk/java/math/BigInteger/`）：`ModPow.java`、`PrimeTest.java`、`ByteArrayConstructorTest.java`、`CompareToTests.java`、`PrimitiveConversionTests.java` / `TestValueExact.java`。
- JDK 源码自用：`java/lang/Long.java`、`java/text/DecimalFormat.java`、`java/util/UUID` 使用 `BigInteger` 处理无符号大数。
- 本仓库两侧 jdk8u372-b07 / jdk-27.0.1.0.2 探针程序实测（/tmp/lcQ-probe，2026-11）。

## 4. 测试用例设计 + 自动化实现
见 `BigIntegerTest.java` 的 `testWhiteBox/testBlackBox`（原有白盒+黑盒）与下节契约深化。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 构造精确性：byte[] 二补码矩阵（{0xFE,0x02}→-510、{0xFF,0x02}→-254、{-128}、{0,0}）；toByteArray 补码 round-trip（255→{00,FF}、-254→{FF,02}）；signum-magnitude 归一化；radix 构造（"7fffffffffffffff"/16、36 进制往返、基数 5 的 "2010"）。
- 位操作契约表：bitLength（-1→0、-256→8、-257→9）、bitCount（-1→0）、负数二补码（5&-3==5、-1>>10==-1、-1.testBit(1000)==true 符号扩展、-2.setBit(0)==-1）。
- mod vs remainder 分叉（招牌）：-3 mod 5==2 vs -3 remainder 5==-3；除法四象限向零截断表。
- 数论：费马小定理（2^10 mod 11==1、3^6 mod 7==1，modPow 与 pow().mod() 双路一致）；modInverse 矩阵 + 逆元自验证 + "BigInteger not invertible."；gcd 表（gcd(0,0)==0、gcd(1071,462)==21）。
- sqrt 家族（JDK-9+）：sqrt(9)==3、sqrt(10/15)==3（floor）、sqrt(16)==4、sqrt(2^64)==2^32、sqrtAndRemainder(15)==[3,6]、sqrt(-4) → "Negative BigInteger" 逐字。
- 精确消息逐字表："BigInteger divide by zero"、"Negative exponent"、"BigInteger: modulus not positive"、"Negative bit address"、"BigInteger out of long/int/short/byte range"、构造 NFE 全家（"For input string: \"x10\""、"Zero length BigInteger"、"signum-magnitude mismatch"、"Invalid signum value"、"Radix out of range"）——两侧逐字一致。
- 素性：isProbablePrime(-7)==true（先 abs 判定）、certainty<=0 恒 true。
- 缓存/收窄：valueOf 0..16 常量池；byteValue/shortValue JLS 5.1.3 截断；hashCode==数值本身。
- 序列化补充：toString/signum/hashCode 保留 + Long.MIN 边界。

### 2. 使用方式
`checkMsg(Class,message,runnable,msg)` 逐字锁异常消息；新增 `testConstructorExactness/testBitContracts/testModularContracts/testBigArithmeticAndMessages/testSqrtFamilyAndPrimality/testComparisonAndSerializationExtra/testCacheAndTwoComplementSemantics/testRadixAndByteArrayMatrix` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧探针实测；Oracle javadoc；JDK 源码 BigInteger.java 消息常量。

### 4. 测试用例设计 + 易错规范细节
- 断言数 218 → 463。易错点实测记录：①{0xFE,0x02} 是 -510 不是 -254；②sqrt 负数消息是 "Negative BigInteger"；③bitLength(-1)==0；④isProbablePrime 对负素数返回 true；⑤valueOf 常量池边界 16；⑥BigInteger.TWO 在 27 侧公开可用；⑦全部异常消息与 JDK-8 逐字一致。
