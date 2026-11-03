# java.security.spec.X509EncodedKeySpec 测试设计

## 1. API 功能
`X509EncodedKeySpec` 继承 `EncodedKeySpec` 并实现 `BinaryEncodable`，表示按 X.509 SubjectPublicKeyInfo 格式编码的公钥。getFormat 固定返回 "X.509"；getEncoded 返回编码字节的防御性拷贝；可选的 2 参构造可附加算法名供 getAlgorithm 返回。

## 2. Java 用户如何使用
```java
byte[] encoded = publicKey.getEncoded();
X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded, "RSA");
String fmt = spec.getFormat();      // "X.509"
byte[] copy = spec.getEncoded();    // 防御性拷贝
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/PEM/PEMEncoderTest.java`（第 235 行）:
  ```java
  encoder.encodeToString(new X509EncodedKeySpec(kp.getPublic().getEncoded()));
  ```
- `test/jdk/java/security/cert/X509CertSelectorTest.java`（构造 X509 公钥规范）:
  ```java
  X509EncodedKeySpec keySpec = new X509EncodedKeySpec(...);
  ```
- JDK 源码 `X509EncodedKeySpec.java` 中 getFormat：
  ```java
  public final String getFormat() { return "X.509"; }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 EncodedKeySpec、实现 BinaryEncodable、含 getEncoded/getFormat 方法。
- 黑盒（testBlackBox）：
  - getFormat 为 "X.509"。
  - getEncoded 返回内容相等的新数组；修改返回数组不影响后续取值；修改构造入参数组不影响内部值（双重防御性拷贝）。
  - 单参构造 getAlgorithm 为 null；2 参构造 getAlgorithm 为传入算法名。
  - `instanceof EncodedKeySpec`。
