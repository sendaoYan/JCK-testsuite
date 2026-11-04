# KeyPairGenerator — 设计案

## 1. API 功能
密钥对生成器抽象类（extends KeyPairGeneratorSpi）。getInstance 工厂 + initialize(int)/initialize(int,SecureRandom)/initialize(AlgorithmParameterSpec)/generateKeyPair/getAlgorithm/getProvider。

## 2. 使用方式
`KeyPairGenerator.getInstance("RSA")` → `initialize(1024)` → `generateKeyPair()` 得非空公/私钥（X.509 / PKCS#8）。

## 3. 参考来源
- javadoc `java.security.KeyPairGenerator`（initialize 负值→InvalidParameterException；spec 非法→InvalidAlgorithmParameterException）。
- OpenJDK `test/jdk/java/security/KeyPairGenerator/`。
- JCK runtime-8b `api/java_security/KeyPairGenerator/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→NSAE；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- 生成结果：公钥 X.509 / 私钥 PKCS#8，算法 RSA，getEncoded 非平凡长度。
- initialize 边界：`initialize(-1)`→InvalidParameterException（JDK-8 与 27 顶层类型一致）；`initialize(非 RSAKeyGenParameterSpec)`→InvalidAlgorithmParameterException；`initialize(null spec)`→InvalidAlgorithmParameterException。
- 三种 initialize 重载（int / int+SecureRandom / spec）均验证。
- 易错点：JDK-27 负 keysize 是 InvalidParameterException 包装 InvalidAlgorithmParameterException，仅断言顶层类型。
