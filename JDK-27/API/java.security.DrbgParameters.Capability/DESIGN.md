# java.security.DrbgParameters.Capability

## 1. API 功能
DRBG 的重播种与预测抵抗能力枚举：`PR_AND_RESEED`（预测抵抗 + 重播种）、`RESEED_ONLY`（仅重播种）、`NONE`（两者皆无）。提供 `supportsReseeding()` 与 `supportsPredictionResistance()`，`toString()` 返回小写常量名。

## 2. Java 用户如何使用
用户把 `Capability` 传入 `DrbgParameters.instantiation(strength, capability, personalization)` 表达对 DRBG 能力的最低要求，或从 `SecureRandom.getParameters()` 返回的 `Instantiation` 读取实际能力。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/DrbgParameters.java`（JDK 源）：
  - `public boolean supportsReseeding() { return this != NONE; }`
  - `public boolean supportsPredictionResistance() { return this == PR_AND_RESEED; }`
  - `public String toString() { return name().toLowerCase(Locale.ROOT); }`
- `test/jdk/java/security/SecureRandom/DrbgParametersSpec.java`：
  - `Asserts.assertTrue(PR_AND_RESEED.supportsPredictionResistance()); Asserts.assertTrue(PR_AND_RESEED.supportsReseeding()); Asserts.assertFalse(NONE.supportsReseeding());`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `DrbgParameters$Capability` 为 public enum、超类 `Enum`、含 3 个常量、含 `valueOf`/`values`/`supportsReseeding`/`supportsPredictionResistance`/`toString`。
- `testBlackBox()`：断言三个常量的 supports 谓词、`valueOf` 往返一致、`toString()` 为小写名（`pr_and_reseed`/`reseed_only`/`none`）。
