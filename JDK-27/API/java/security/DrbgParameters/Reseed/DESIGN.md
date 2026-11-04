# java.security.DrbgParameters.Reseed

## 1. API 功能
DRBG 重播种参数，实现 `SecureRandomParameters`。字段为 predictionResistance、additionalInput。`getPredictionResistance()` 返回是否请求预测抵抗，`getAdditionalInput()` 每次返回新拷贝（null 表示无附加输入）。

## 2. Java 用户如何使用
用户通过 `DrbgParameters.reseed(true, "extra".getBytes())` 构造并传给 `SecureRandom.reseed(SecureRandomParameters)`；不带附加输入时传 `null`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/DrbgParameters.java`（JDK 源）：
  - `public byte[] getAdditionalInput() { return additionalInput == null ? null : additionalInput.clone(); }`
  - `private Reseed(boolean predictionResistance, byte[] additionalInput) { this.predictionResistance = predictionResistance; this.additionalInput = (additionalInput == null) ? null : additionalInput.clone(); }`
- `test/jdk/java/security/SecureRandom/DrbgParametersSpec.java`：
  - `DrbgParameters.Reseed rs = DrbgParameters.reseed(true, p); np1 = rs.getAdditionalInput(); np2 = rs.getAdditionalInput(); Asserts.assertNE(np1, np2);`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `DrbgParameters$Reseed` 为 public final class、实现 `SecureRandomParameters`、含 `getPredictionResistance`/`getAdditionalInput`。
- `testBlackBox()`：断言 `reseed(false, null)` 的 PR 为 false 且附加输入为 null；`reseed(true, {7})` 的 PR 为 true、附加输入内容一致且 `getAdditionalInput()` 为防御性拷贝。
