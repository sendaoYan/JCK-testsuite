# java.security.spec.MGF1ParameterSpec 测试设计

## 1. API 功能
`MGF1ParameterSpec` 实现 `AlgorithmParameterSpec`，指定掩码生成函数 MGF1 使用的摘要算法。提供 11 个预定义常量（SHA1/SHA224/SHA256/SHA384/SHA512/SHA512_224/SHA512_256/SHA3_224/SHA3_256/SHA3_384/SHA3_512），以及 `String` 构造器和 getDigestAlgorithm/toString。

## 2. Java 用户如何使用
```java
PSSParameterSpec pss = new PSSParameterSpec(
    "SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1);
MGF1ParameterSpec sha256 = MGF1ParameterSpec.SHA256;
String digest = sha256.getDigestAlgorithm();  // "SHA-256"
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/xml/crypto/dsig/PSSSpec.java`（与 PSSParameterSpec 组合）:
  ```java
  private static final PSSParameterSpec DEFAULT_SPEC =
      new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"),
                           32, PSSParameterSpec.TRAILER_FIELD_BC);
  ```
- `test/jdk/com/sun/crypto/provider/Cipher/RSA/TestOAEPParameterSpec.java`（OAEP 中读取 MGF 摘要）:
  ```java
  MGF1ParameterSpec mp1 = (MGF1ParameterSpec)s1.getMGFParameters();
  alg1 = mp1.getDigestAlgorithm();
  ```
- JDK 源码 `MGF1ParameterSpec.java` 中常量定义：
  ```java
  public static final MGF1ParameterSpec SHA256 = new MGF1ParameterSpec("SHA-256");
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 AlgorithmParameterSpec、含 getDigestAlgorithm/toString 方法。
- 黑盒（testBlackBox）：
  - `String` 构造后 getDigestAlgorithm 精确回读。
  - toString 包含类名与摘要名。
  - 11 个常量的 getDigestAlgorithm 逐一精确断言（含 SHA-512/224、SHA3-* 等）。
  - `instanceof AlgorithmParameterSpec`。
