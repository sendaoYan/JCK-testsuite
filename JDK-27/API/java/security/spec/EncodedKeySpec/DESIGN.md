# java.security.spec.EncodedKeySpec 测试设计

## 1. API 功能
`EncodedKeySpec` 是抽象类，实现 `KeySpec`，表示以编码字节数组形式提供的密钥规范。它保存编码密钥的字节拷贝与算法名（单参构造时算法名为 null）。`getEncoded()` 每次返回新的字节数组拷贝；`getFormat()` 为抽象方法，由子类返回具体编码格式；`getAlgorithm()` 返回算法名。

## 2. Java 用户如何使用
该类不直接实例化，通过 `X509EncodedKeySpec`（公钥）与 `PKCS8EncodedKeySpec`（私钥）等子类使用：
```java
byte[] enc = key.getEncoded();
X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(enc, "RSA");
byte[] copy = pubSpec.getEncoded();   // 防御性拷贝
String fmt = pubSpec.getFormat();     // "X.509"
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/PEM/PEMEncoderTest.java`（第 235-236 行）:
  ```java
  encoder.encodeToString(new X509EncodedKeySpec(kp.getPublic().getEncoded()));
  encoder.encodeToString(new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded()));
  ```
- `test/jdk/java/security/spec/PKCS8EncodedKeySpec/Algorithm.java`（2 参构造 + 取算法名）:
  ```java
  PKCS8EncodedKeySpec keySpec = epki.getKeySpec(sk);
  // Get the key algorithm and make sure it's what we expect
  ```
- JDK 源码 `EncodedKeySpec.java` 中 `getEncoded()`:
  ```java
  public byte[] getEncoded() { return this.encodedKey.clone(); }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 抽象类、父类 Object、实现 KeySpec、含 getAlgorithm/getEncoded/getFormat 方法，且 getFormat 为 abstract。
- 黑盒（testBlackBox）：借助 X509EncodedKeySpec/PKCS8EncodedKeySpec 子类验证：
  - 2 参构造 getAlgorithm 返回传入算法名（非空）；单参构造返回 null。
  - getEncoded 返回新数组（防御性拷贝），修改返回数组不影响后续取值。
  - 子类 `instanceof EncodedKeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：构造 null→NPE
- 关键实测：抽象基类，持有编码字节的私有拷贝，定义 getEncoded（防御拷贝）与抽象 getFormat；子类 PKCS8EncodedKeySpec/X509EncodedKeySpec 指定格式名。JDK-9+ 增加 (byte[],String algorithm) 受保护构造器与 getAlgorithm()。
KeyFactory 以 X509EncodedKeySpec/PKCS8EncodedKeySpec 包装 DER 编码往返还原密钥。
