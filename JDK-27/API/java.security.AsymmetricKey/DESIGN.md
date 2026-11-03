# java.security.AsymmetricKey

## 1. API 功能
非对称密钥（公钥/私钥）的统一接口，扩展 `Key` 和 `BinaryEncodable`（JDK 25 中名为 `DEREncodable`）。提供默认方法 `getParams()`，默认返回 `null`，用于让实现类返回其算法参数。

## 2. Java 用户如何使用
用户对所有公钥/私钥（RSA/EC/DSA/DH/EdDSA/XDH 等）通过 `key.getParams()` 获取 `AlgorithmParameterSpec`；当算法不携带参数时返回 `null`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/AsymmetricKey.java`（JDK 源）：`public non-sealed interface AsymmetricKey extends Key, DEREncodable { default AlgorithmParameterSpec getParams() { return null; } }`
- `test/jdk/java/security/AsymmetricKey/GetParams.java`：
  - `KeyPairGenerator g = KeyPairGenerator.getInstance(alg); KeyPair kp = g.generateKeyPair();`
  - `AlgorithmParameterSpec spec1 = kp.getPrivate().getParams(); AlgorithmParameterSpec spec2 = kp.getPublic().getParams();`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public 接口、无超类、实现 `java.security.Key` 与 `java.security.BinaryEncodable`、含 `getParams` 方法。
- `testBlackBox()`：用 `KeyPairGenerator.getInstance("RSA")` 初始化 2048 生成真实 RSA 密钥对，断言公私钥均为 `AsymmetricKey` 实例且 `getParams()` 返回 `null`。
