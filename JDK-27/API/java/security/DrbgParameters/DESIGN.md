# java.security.DrbgParameters

## 1. API 功能
DRBG（确定性随机比特生成器）参数工具类，含私有构造器，只提供三个静态工厂方法：`instantiation(int, Capability, byte[])`、`nextBytes(int, boolean, byte[])`、`reseed(boolean, byte[])`，分别返回 `Instantiation`、`NextBytes`、`Reseed` 参数对象。

## 2. Java 用户如何使用
用户通过工厂方法构造参数并传给 `SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(...))`、`SecureRandom.nextBytes(byte[], DrbgParameters.nextBytes(...))`、`SecureRandom.reseed(DrbgParameters.reseed(...))`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/DrbgParameters.java`（JDK 源）：
  - `public static Instantiation instantiation(int strength, Capability capability, byte[] personalizationString) { return new Instantiation(strength, Objects.requireNonNull(capability), personalizationString); }`
  - `public static NextBytes nextBytes(int strength, boolean predictionResistance, byte[] additionalInput) { ... }`
  - `public static Reseed reseed(boolean predictionResistance, byte[] additionalInput) { ... }`
- `test/jdk/java/security/SecureRandom/DrbgParametersSpec.java`：`DrbgParameters.Instantiation ins = DrbgParameters.instantiation(192, RESEED_ONLY, p);`、`DrbgParameters.NextBytes nb = DrbgParameters.nextBytes(192, true, p);`、`DrbgParameters.Reseed rs = DrbgParameters.reseed(true, p);`
- `test/jdk/java/security/SecureRandom/ApiTest.java`：`SecureRandomParameters param = DrbgParameters.instantiation(strength, cp, pr);`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public final class、超类 `Object`、无 public 构造器、含 `instantiation`/`nextBytes`/`reseed` 静态方法。
- `testBlackBox()`：断言三个工厂返回的对象字段正确（strength/capability/PR/个性化串/附加输入），且均为 `SecureRandomParameters` 实例。
