# SecureRandom — 设计案

## 1. API 功能
密码学安全随机数生成器。getInstance 工厂 + 无参/带种子构造 + nextBytes/generateSeed/setSeed/getAlgorithm/getProvider。

## 2. 使用方式
`SecureRandom.getInstance("SHA1PRNG")` → `setSeed(seed)` → `nextBytes(buf)`；`generateSeed(n)` 取熵种子。

## 3. 参考来源
- javadoc `java.security.SecureRandom`。
- OpenJDK `test/jdk/java/security/SecureRandom/`（GetInstance、GetAlgorithm）。
- JCK runtime-8b `api/java_security/SecureRandom/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→NSAE；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- 非全零：固定 100 字节缓冲 nextBytes 有界非全零（不依赖墙钟）。
- null：nextBytes(null)→NPE；setSeed(null)→NPE；setSeed(byte[]) 合法。
- 可复现：两个 SHA1PRNG 实例同 setSeed 后输出一致（JDK-8 与 27 均验证 true）。
- 边界：generateSeed(20) 长度正确；generateSeed(-1)→JDK-8 NegativeArraySizeException / JDK-27 IllegalArgumentException（镜像时按各 JDK 适配）。
- 易错点：nextBytes 是"有界非全零"而非"确定性"；SHA1PRNG 在两 JDK 均可用。
