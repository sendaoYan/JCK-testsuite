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
