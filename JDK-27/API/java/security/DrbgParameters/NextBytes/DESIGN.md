# java.security.DrbgParameters.NextBytes

## 1. API 功能
DRBG 生成随机字节的参数，实现 `SecureRandomParameters`。字段为 strength、predictionResistance、additionalInput。`getStrength()` 返回请求强度（-1 表示使用有效强度），`getPredictionResistance()` 返回是否请求预测抵抗，`getAdditionalInput()` 每次返回新拷贝。

## 2. Java 用户如何使用
用户通过 `DrbgParameters.nextBytes(256, true, "more".getBytes())` 构造并传给 `SecureRandom.nextBytes(byte[], SecureRandomParameters)`；`strength < -1` 抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/DrbgParameters.java`（JDK 源）：
  - `public byte[] getAdditionalInput() { return additionalInput == null ? null : additionalInput.clone(); }`
  - `private NextBytes(int strength, boolean predictionResistance, byte[] additionalInput) { if (strength < -1) throw new IllegalArgumentException(...); ... }`
- `test/jdk/java/security/SecureRandom/DrbgParametersSpec.java`：
  - `DrbgParameters.NextBytes nb = DrbgParameters.nextBytes(192, true, p); np1 = nb.getAdditionalInput(); np2 = nb.getAdditionalInput(); Asserts.assertNE(np1, np2);`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `DrbgParameters$NextBytes` 为 public final class、实现 `SecureRandomParameters`、含 `getStrength`/`getPredictionResistance`/`getAdditionalInput`。
- `testBlackBox()`：断言 strength/PR/附加输入取值、`getAdditionalInput()` 防御性拷贝、`-1` 强度与 null 附加输入、`strength < -1` 抛 `IllegalArgumentException`。
