# java.security.SecureRandomParameters

## 1. API 功能
一个标记接口（marker interface），用于表示传给 `SecureRandom` 的参数对象。DRBG 相关的 `DrbgParameters.Instantiation`、`DrbgParameters.NextBytes`、`DrbgParameters.Reseed` 均实现该接口。

## 2. Java 用户如何使用
用户以 `SecureRandomParameters` 类型把 `DrbgParameters.instantiation(...)` 等结果传给 `SecureRandom.getInstance(String, SecureRandomParameters)`、`SecureRandom.nextBytes(byte[], SecureRandomParameters)` 或 `SecureRandom.reseed(SecureRandomParameters)`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/SecureRandomParameters.java`（JDK 源）：空接口，仅含 Javadoc。
- `src/java.base/share/classes/java/security/DrbgParameters.java`（JDK 源）：`public static final class Instantiation implements SecureRandomParameters`、`NextBytes`、`Reseed` 同。
- `test/jdk/java/security/SecureRandom/ApiTest.java`：
  - `SecureRandomParameters param = DrbgParameters.instantiation(strength, cp, pr);`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public 接口且无超类。
- `testBlackBox()`：断言 `DrbgParameters.instantiation(...)`、`DrbgParameters.nextBytes(...)`、`DrbgParameters.reseed(...)` 三者均为 `SecureRandomParameters` 实例。
