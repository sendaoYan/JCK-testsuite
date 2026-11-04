# java.security.SecureRandomSpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`SecureRandom` 外观的 SPI 后端；实现 `Serializable`；public 无参构造。
- JDK-8 恰好 3 个抽象 protected 方法：`engineSetSeed(byte[])`、`engineNextBytes(byte[])`、`engineGenerateSeed(int)`。
- **JDK-8 不声明 toString()（用 Object 默认）；JDK-9+ 新增带参构造与 engineNextBytes(byte[],SecureRandomParameters)/engineReseed/engineGetParameters/toString**。

## 2. Java 用户如何使用
provider 继承 SecureRandomSpi，注册后由 `SecureRandom.getInstance("SHA1PRNG")` 装配；setSeed/nextBytes/generateSeed 经外观转发。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/SecureRandomSpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/SecureRandom/`
- SUN 实现 `sun.security.provider.SecureRandom`

## 4. 测试用例设计 + 自动化实现
- 结构：Serializable、exact-3-abstract、**不声明 toString（与 JDK-9+ 形成版本对照）**。
- 透明记录子类：setSeed（克隆记录内容）、nextBytes（子类填充 0x5A）、generateSeed（记录长度）精确透传。
- 序列化：SPI 是 Serializable，子类 ObjectOutputStream 往返后仍可用（JDK-27 同）。
- 外观闭环（真实 SUN SHA1PRNG SPI）：两个实例先 `setSeed(42L)` 再 nextBytes 输出逐位相等（确定性，SUN 实现契约）；`generateSeed(8)` 长度 8；`new SecureRandom()` 默认实例含 provider/algorithm 且 nextBytes 正常返回。

断言入口：`SecureRandomSpiTest.testContractDeep()`（7→23 断言）。
