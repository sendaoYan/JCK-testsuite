# AlgorithmParameterGenerator — 设计案

## 1. API 功能
生成算法参数（AlgorithmParameters）。getInstance(算法[, 提供者]) + getAlgorithm/getProvider + init(大小/参数规范[, SecureRandom]) + generateParameters。

## 2. 使用方式
`AlgorithmParameterGenerator.getInstance("DiffieHellman")` 或 `"DSA"`（JDK-8 标准实现仅支持这两种），init 后 `generateParameters()`。

## 3. 参考来源
- javadoc `java.security.AlgorithmParameterGenerator`。
- JCK runtime-8b `api/java_security/AlgorithmParameterGenerator/`。

## 4. 测试用例设计 + 易错规范细节
- 标准算法：DiffieHellman（SunJCE）、DSA（SUN）；AES/DES/RSA 无 AlgorithmParameterGenerator 实现。
- getInstance(null)→NPE；getInstance("")→NoSuchAlgorithmException；getInstance("NoSuch")→NoSuchAlgorithmException。
- getInstance(算法, (Provider)null)→IllegalArgumentException；getInstance(null, "SUN")→NPE。
- init(int)+generateParameters（DH 512 / DSA 1024）；init(DHGenParameterSpec)（DH）；init(int, SecureRandom)（DSA）。
- 生成参数 getAlgorithm 分别为 "DH"/"DSA"；DSA 参数可解出 DSAParameterSpec（p/q/g 非空、p 位数=请求值）。
- init((AlgorithmParameterSpec)null)→InvalidAlgorithmParameterException；init(-1)→InvalidParameterException。
- 易错点：DHGenParameterSpec 在 javax.crypto.spec 包；getInstance 的 provider 重载为 IllegalArgumentException 而非 NPE。
