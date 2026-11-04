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

## 5. 契约深化补充（本批）
- DSA：1024 位 p/q=1024/160 精确、两次生成不等、spec 驱动生成复用 p/q/g、非支持尺寸(1023/64/0/负) → InvalidParameterException；默认 genKeyPair p=2048。
- RSA：513 位奇尺寸兑现、默认指数 65537、RSAKeyGenParameterSpec 自定义 F0 指数兑现；RSA 默认尺寸差异（2048@JDK-8 vs 3072@JDK-27）不入断言。
- EC：ECGenParameterSpec(secp256r1) 与 initialize(256) 均产出 256 位域曲线；未知曲线 → InvalidAlgorithmParameterException；initialize(100) → InvalidParameterException。
- initialize(size, null SecureRandom) 合法；生成后可再 initialize 换尺寸。
