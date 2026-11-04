# java.security.interfaces.DSAParams — 设计案

## 1. API 功能
DSA 域参数接口：getP/getQ/getG 三个 BigInteger。JDK-8 中无父接口（JDK-9+ 起 extends AlgorithmParameterSpec，本侧不适用）。

## 2. 使用方式
从 DSAKey 取出参数读 p/q/g；或用 DSAParameterSpec 包装后在 KeyPairGenerator.initialize 复用。

## 3. 参考来源
- javadoc `java.security.interfaces.DSAParams`（FIPS 186 域参数语义）。
- OpenJDK `test/jdk/java/security/KeyPairGeneratorGenerator` DSA 参数用例思路。

## 4. 测试用例设计 + 易错规范细节
- 记录型最小实现：p/q/g 恒等透传 + null 透传。
- 继承格：无父接口、非 Key、非 Serializable、无 serialVersionUID（JDK-8）。
- 真实数学契约（DSA-1024）：p 1024 位素数、q 160 位素数（isProbablePrime(64)）、g ∈ (0,p) 且 g^q mod p == 1（g 的阶整除 q）、公私两侧参数一致。
- 参数复用：DSAParameterSpec 包装 → 再生成 → 参数逐字段不变。
- 易错点：isProbablePrime(64) 对 DSA 参数必然成立（生成器保证），但注意它是概率性测试；g^q mod p 方程是确定性断言。
