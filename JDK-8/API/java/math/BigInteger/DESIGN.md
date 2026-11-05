# java.math.BigInteger 测试方案

## 1. API 功能（按方法分组，依据 JDK-8 `javap`）
- 构造/工厂：`BigInteger(byte[])`、`BigInteger(int,byte[])`、`BigInteger(String[,int])`、
  `BigInteger(int,Random)`、`BigInteger(int,int,Random)`、`probablePrime(int,Random)`、`valueOf(long)`、
  `nextProbablePrime()`。
- 算术：`add/subtract/multiply/divide/divideAndRemainder/remainder/pow`。
- 符号/gcd：`gcd/abs/negate/signum`。
- 模运算：`mod/modPow/modInverse`。
- 位运算：`shiftLeft/shiftRight/and/or/xor/not/andNot/testBit/setBit/clearBit/flipBit/
  getLowestSetBit/bitLength/bitCount`。
- 素性：`isProbablePrime`。
- 比较/对象：`compareTo/equals/hashCode/min/max`。
- 转换：`toString([int])/toByteArray/intValue/longValue/floatValue/doubleValue/*ValueExact`。
- 常量：`ZERO/ONE/TEN`。

## 2. Java 用户如何使用
```java
BigInteger n = new BigInteger("12345678901234567890");  // 任意精度整数
BigInteger m = n.modPow(exp, modulus);                  // RSA 类密码运算
byte[] bytes = n.toByteArray();                         // 序列化
boolean prime = n.isProbablePrime(50);                  // 素性测试
```

## 3. 开源代码实际用法调研
来源（JDK 自带权威测试，未用 WebSearch，按回退规则标注）：
- `~/git/jdk8u-dev/jdk/test/java/math/BigInteger/`（官方回归：divide/bit ops/modPow 边界）。
- `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_math/BigInteger`（JCK 边界矩阵）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/math/BigInteger.java`（异常契约：ArithmeticException/
  NumberFormatException/IllegalArgumentException 的精确类型）。

## 4. 测试用例设计 + 自动化实现
推导场景（对应 `BigIntegerTest.java`）：
- 构造/工厂：String（前导 +/负/大数）、各 radix、byte[] 补码、signum+mag、valueOf、probablePrime（固定 seed）。
- null 语义：构造器与全部二元运算/位运算 null → NPE。
- 算术+不可变性：add/subtract/multiply/divide 截断、divideAndRemainder、remainder 负数、pow（含 0^0==1）、操作数不变。
- gcd/abs/negate/signum（负数、0）。
- 模运算：mod（负数取非负余）、modPow、modInverse（含 m==1 返回 0）。
- 位运算：全通道 + 负位移距离反转 + getLowestSetBit(-1) + bitLength + bitCount 负数补码语义 + 不可变性。
- 素性：2/97/合数/0/1/nextProbablePrime。
- 比较/对象：compareTo 反对称、equals/hashCode、equals(null)/异类型、min/max。
- 转换：toString 各 radix、toByteArray、*ValueExact 溢出 → ArithmeticException。
- 序列化：serialVersionUID(-8287574255936472291L) + round-trip。
- 异常矩阵：空串/坏数字/空 byte[]/signum 越界/radix 越界/numBits<0(IAE)/bitLength<2(ArithmeticException)/
  除零/负指数/mod 非正/modInverse 不可逆/负位地址。
- 确定性：固定输入、固定 seed 的 Random；无墙钟/随机/Locale/TimeZone；headless-safe（无 `@key`）。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 构造精确性：byte[] 二补码矩阵（{0xFE,0x02}→-510、{0xFF,0x02}→-254、{-128}、{0,0}、前导零）；toByteArray 补码 round-trip（255→{00,FF}、-254→{FF,02}、5→{5}、-1→{FF}）；signum-magnitude 归一化（零 mag → 0、前导零剥离）；radix 构造（"7fffffffffffffff"/16、36 进制往返）。
- 位操作契约表：bitLength（0→0、-1→0、-256→8、-257→9）、bitCount（-1→0）、getLowestSetBit（-12→2）、负数二补码（5&-3==5、-1&0xFF、-1>>10==-1、-1.testBit(1000)==true 符号扩展、-2.setBit(0)==-1）。
- mod vs remainder 分叉（招牌）：-3 mod 5==2 vs -3 remainder 5==-3；-7/5：3 vs -2；除法四象限向零截断表（-7/2、7/-2、-7/-2）。
- 数论：费马小定理（2^10 mod 11==1、3^6 mod 7==1，modPow 与 pow().mod() 双路一致性）；modInverse（3 mod 7→5、4 mod 9→7、11 mod 29→8）+ 逆元自验证 + gcd(6,8)=2 无逆 "BigInteger not invertible."；gcd 表（gcd(0,0)==0、gcd(1071,462)==21）。
- 大数算术：20 位十进制乘/加/减/除/余全等字符串；2^64、2^31、10^20 全等。
- 精确消息逐字表：divide/remainder/divideAndRemainder by zero → "BigInteger divide by zero"；pow 负指数 → "Negative exponent"；mod/modPow/modInverse 非正模 → "BigInteger: modulus not positive"；位地址负 → "Negative bit address"；longValueExact/intValueExact/shortValueExact/byteValueExact 溢出 → "BigInteger out of long/int/short/byte range"；构造 NFE → "For input string: \"x10\""、\"12a\"、"Zero length BigInteger"、"signum-magnitude mismatch"、"Invalid signum value"、"Radix out of range"。
- 素性：isProbablePrime(-7)==true（实现先 abs 判定，实测锁定）、certainty<=0 恒 true。
- 缓存/收窄：valueOf 0..16 常量池（17 起新实例）；byteValue/shortValue JLS 5.1.3 截断（257→1、-129→127、70000→4464）；hashCode==数值本身。
- 序列化补充：toString/signum/hashCode 保留 + Long.MIN 边界。

### 2. 使用方式
`checkMsg(Class,message,runnable,msg)` 逐字锁异常消息；新增 `testConstructorExactness/testBitContracts/testModularContracts/testBigArithmeticAndMessages/testPrimalityMatrix/testComparisonAndSerializationExtra/testCacheAndTwoComplementSemantics/testRadixAndByteArrayMatrix` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧 jdk8u372-b07 / jdk-27.0.1.0.2 探针实测；Oracle javadoc（二补码 byte[] 语义、mod 与 remainder 差、bitLength 定义）；JDK 源码 BigInteger.java 消息常量。

### 4. 测试用例设计 + 易错规范细节
- 断言数 162 → 401。易错点实测记录：①{0xFE,0x02} 是 -510 不是 -254（-254 是 {0xFF,0x02}）；②BigInteger.TWO 在 JDK-8 为 private（9+ 才公开）——8 侧用 valueOf(2)；③bitLength(-1)==0（契约：-x 的位长 == x-1 的位长）；④isProbablePrime 对负素数返回 true（先 abs）；⑤valueOf 常量池边界 16；⑥全部异常消息两侧逐字一致；⑦sqrt/sqrtAndRemainder 在 JDK-8 不存在（javac 即证），8 侧不测。
