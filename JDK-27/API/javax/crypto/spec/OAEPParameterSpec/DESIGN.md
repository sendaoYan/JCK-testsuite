# javax.crypto.spec.OAEPParameterSpec

## 1. API 功能
RSA-OAEP（PKCS#1 v2.2）加密的参数容器：摘要算法 `digestAlgorithm`、掩码生成函数 `mgfAlgorithm`（"MGF1"）、MGF 参数 `mgfSpec`（通常 `MGF1ParameterSpec`）与编码输入源 `PSource`（label）。一个 4 参构造器 + 4 个 getter + 静态常量 `DEFAULT`（JDK-19 起 `@Deprecated`：SHA-1 相关）。实现 `AlgorithmParameterSpec`。

## 2. Java 用户如何使用
`new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)` 后传给 `Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding").init(mode, key, spec)`；自定义 label 用 `new PSource.PSpecified(label)`；getter 回读四元组；`mgfSpec=null` 时 `getMGFParameters()` 返回 null。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/sun/security/rsa/TestOAEPWithParams.java:52-60`：
  ```java
  OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1",
      MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
  c.init(Cipher.ENCRYPT_MODE, pubKey, spec);
  ```
- `~/git/jdk25u-dev/test/jdk/sun/security/rsa/TestOAEPKAT.java`：OAEP KAT 向量（SHA-1/SHA-256 + 不同 label）。
- BouncyCastle 常见 `OAEPParameterSpec(sha256, "MGF1", MGF1ParameterSpec.SHA256, new PSource.PSpecified(label))`（GitHub 高频用法，如 JOSE/Tink F4M RSA-OAEP 实现）。
- 源码 `src/java.base/share/classes/javax/crypto/spec/OAEPParameterSpec.java`：null 参数构造抛 NPE（Objects.requireNonNull）。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：public、具体非 final、直接父类 Object、实现 `AlgorithmParameterSpec`；4 参公共构造器签名；`DEFAULT` 为 public static final、类型为自身且 `@Deprecated`；`getDigestAlgorithm/getMGFAlgorithm/getMGFParameters/getPSource` 公共方法存在。
- `testBlackBox()`：
  - 保留旧断言（DEFAULT 非 null、自定义 spec 四 getter）。
  - `DEFAULT` 内容 = SHA-1 + MGF1 + `MGF1ParameterSpec.SHA1` + `PSpecified.DEFAULT`。
  - 自定义 SHA-512 + 8 字节 label 的 getter 全回读（mgf 参数内容、pSource 同一引用与 label 字节）。
  - `mgfSpec=null` 原样回传 null；digest/mgf/pSrc 传 null → NullPointerException。
  - 真实链路：RSA-2048 `RSA/ECB/OAEPWithSHA-256AndMGF1Padding` 用带 label 的 spec 加密（密文 256 字节、随机 seed 两次加密不同）→ 同 spec 解密还原精确 32 字节明文；换空 label 或换 SHA-1 digest 解密均抛 `BadPaddingException`。
