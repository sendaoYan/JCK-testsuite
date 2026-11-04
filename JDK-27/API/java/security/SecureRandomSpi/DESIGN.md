# java.security.SecureRandomSpi

## 1. API 功能
密码学强伪随机数生成器的服务提供者接口。实现者需提供 `engineSetSeed(byte[])`、`engineNextBytes(byte[])`、`engineGenerateSeed(int)`。类提供若干默认方法：`engineNextBytes(byte[],SecureRandomParameters)` 与 `engineReseed(SecureRandomParameters)` 默认抛 `UnsupportedOperationException`，`engineGetParameters()` 默认返回 `null`，`toString()` 默认返回实现类的简单类名。

## 2. Java 用户如何使用
用户通过 `SecureRandom.getInstance("DRBG")` 或 `new SecureRandom()` 获取封装实例，底层引擎方法由 `SecureRandom` 调用。提供者子类实现三个抽象方法，必要时覆写带参数的 `engineNextBytes`/`engineReseed` 以支持参数化。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/SecureRandomSpi.java`（JDK 源）：
  - `protected void engineNextBytes(byte[] bytes, SecureRandomParameters params) { throw new UnsupportedOperationException(); }`
  - `protected void engineReseed(SecureRandomParameters params) { throw new UnsupportedOperationException(); }`
  - `protected SecureRandomParameters engineGetParameters() { return null; }`
  - `public String toString() { return getClass().getSimpleName(); }`
- `test/jdk/java/security/SecureRandom/ThreadSafe.java`：
  - `public static class S extends SecureRandomSpi { protected void engineSetSeed(byte[] seed) {...} protected void engineNextBytes(byte[] bytes) {...} protected byte[] engineGenerateSeed(int numBytes) {...} }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public abstract class、超类 `Object`、实现 `java.io.Serializable`、含 `toString` 方法。
- `testBlackBox()`：最小子类 `MyRandomSpi` 将 `engineNextBytes` 填充为 7、`engineGenerateSeed` 填充为 3；断言填充结果、`engineGetParameters()==null`、`toString()=="MyRandomSpi"`，并断言默认 `engineNextBytes(byte[],params)` 与 `engineReseed(params)` 抛 `UnsupportedOperationException`。

## 5. 契约深化（testContractDeep，本轮新增）
- JDK-27 表面：protected 构造器(SecureRandomParameters)；engineNextBytes(byte[],params) 默认恒 UOE（即使 params 为 null 也不回退）；engineReseed(params) 默认 UOE；engineGetParameters() 默认 null；声明 public toString()=简单类名（JDK-8 对照：不声明 toString、仅 3 抽象）。
- 记录子类三方法精确透传；子类序列化往返（SPI 为 Serializable）仍可用。
- 外观闭环：SHA1PRNG 两实例 setSeed(42L) 先行再 nextBytes 输出逐位相等；generateSeed(8) 长度 8。
