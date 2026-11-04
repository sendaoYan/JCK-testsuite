# java.security.BinaryEncodable

## 1. API 功能
标记接口，表示“包含可二进制编码的密钥或证书材料”的安全 API 类。JDK 27 中名为 `BinaryEncodable`（JDK 25 源中名为 `DEREncodable`，且为 `@PreviewFeature(PEM_API)` 的 sealed 接口），其实现类提供与字节数组/DER 格式互转的方法。

## 2. Java 用户如何使用
用户不直接调用该接口，而是通过 `instanceof BinaryEncodable` 判断某对象（如 `PKCS8EncodedKeySpec`、`X509EncodedKeySpec`、`AsymmetricKey`）是否可二进制编码。JDK 27 下该接口属于 preview API，编译需 `--enable-preview`。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/DEREncodable.java`（JDK 源，JDK 25 中的前身名称）：
  - `public sealed interface DEREncodable permits AsymmetricKey, KeyPair, PKCS8EncodedKeySpec, X509EncodedKeySpec, EncryptedPrivateKeyInfo, X509Certificate, X509CRL, PEMRecord { }`
- `src/java.base/share/classes/java/security/spec/PKCS8EncodedKeySpec.java`（JDK 源）：`public non-sealed class PKCS8EncodedKeySpec extends EncodedKeySpec implements DEREncodable`
- `src/java.base/share/classes/java/security/spec/X509EncodedKeySpec.java`（JDK 源）：`public non-sealed class X509EncodedKeySpec extends EncodedKeySpec implements DEREncodable`
- JDK 27 下经 `javap` 确认：`PKCS8EncodedKeySpec`/`X509EncodedKeySpec` 声明 `implements java.security.BinaryEncodable`。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public 接口、无超类。
- `testBlackBox()`：断言 `new PKCS8EncodedKeySpec(...)` 与 `new X509EncodedKeySpec(...)` 均为 `BinaryEncodable` 实例（测试文件加 `@enablePreview` 以启用 preview API）。

## 5. 契约深化补充（本批）
- **实现方式**：BinaryEncodable/PEM/AsymmetricKey 均为 JDK-27 preview API，测试改为全反射访问（`@enablePreview` 已移除；普通 javac/jtreg 即可编译运行）。
- 深化断言：纯标记接口（0 声明方法、无超接口、非 Serializable）；`AsymmetricKey` 的直接超接口恰为 {Key, BinaryEncodable}；PKCS#8/X.509 spec 的 format 标签、getEncoded 每次返回副本、null → NPE；RSA 公/私钥是 BinaryEncodable 而 AES SecretKey 不是；PEM 也是；**接口 sealed** 且 permits 列表含 AsymmetricKey（用户代码无法自行实现）。
