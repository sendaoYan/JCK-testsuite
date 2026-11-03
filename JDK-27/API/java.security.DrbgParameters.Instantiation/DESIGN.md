# java.security.DrbgParameters.Instantiation

## 1. API 功能
DRBG 实例化参数，实现 `SecureRandomParameters`。字段为 strength、capability、personalizationString。`getStrength()`/`getCapability()` 返回原始值，`getPersonalizationString()` 每次返回新拷贝（防御性拷贝），`toString()` 返回 `strength,capability,字符串` 形式。

## 2. Java 用户如何使用
用户通过 `DrbgParameters.instantiation(256, Capability.PR_AND_RESEED, "hello".getBytes())` 构造，并传给 `SecureRandom.getInstance("DRBG", params)`；构造时 `strength < -1` 抛 `IllegalArgumentException`，`capability == null` 抛 `NullPointerException`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/DrbgParameters.java`（JDK 源）：
  - `public byte[] getPersonalizationString() { return (personalizationString == null) ? null : personalizationString.clone(); }`
  - `private Instantiation(int strength, Capability capability, byte[] personalizationString) { if (strength < -1) throw new IllegalArgumentException(...); this.personalizationString = (personalizationString == null) ? null : personalizationString.clone(); }`
- `test/jdk/java/security/SecureRandom/DrbgParametersSpec.java`：
  - `DrbgParameters.Instantiation ins = DrbgParameters.instantiation(192, RESEED_ONLY, p); np1 = ins.getPersonalizationString(); np2 = ins.getPersonalizationString(); Asserts.assertNE(np1, np2);`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `DrbgParameters$Instantiation` 为 public final class、实现 `SecureRandomParameters`、含 `getStrength`/`getCapability`/`getPersonalizationString`/`toString`。
- `testBlackBox()`：断言 strength/capability/个性化串取值、`getPersonalizationString()` 防御性拷贝（改动返回值不影响下次读取）、`strength -1` 与 null 个性化串、`strength < -1` 抛 `IllegalArgumentException`、null capability 抛 `NullPointerException`。
