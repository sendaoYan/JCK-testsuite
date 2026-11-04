# java.security.spec.PKCS8EncodedKeySpec 测试设计

## 1. API 功能
`PKCS8EncodedKeySpec` 继承 `EncodedKeySpec` 并实现 `BinaryEncodable`，表示按 PKCS#8 格式编码的私钥。getFormat 固定返回 "PKCS#8"；getEncoded 返回编码字节的防御性拷贝；可选的 2 参构造可附加算法名供 getAlgorithm 返回。

## 2. Java 用户如何使用
```java
byte[] encoded = privateKey.getEncoded();
PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded, "RSA");
String fmt = spec.getFormat();          // "PKCS#8"
byte[] copy = spec.getEncoded();        // 防御性拷贝
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/PEM/PEMEncoderTest.java`（第 236 行）:
  ```java
  encoder.encodeToString(new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded()));
  ```
- `test/jdk/java/security/spec/PKCS8EncodedKeySpec/Algorithm.java`（2 参构造 + 算法名回读）:
  ```java
  PKCS8EncodedKeySpec keySpec = epki.getKeySpec(sk);
  // Get the key algorithm and make sure it's what we expect
  ```
- JDK 源码 `PKCS8EncodedKeySpec.java` 中 getFormat：
  ```java
  public final String getFormat() { return "PKCS#8"; }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 EncodedKeySpec、实现 BinaryEncodable、含 getEncoded/getFormat 方法。
- 黑盒（testBlackBox）：
  - getFormat 为 "PKCS#8"。
  - getEncoded 返回内容相等的新数组；修改返回数组不影响后续取值；修改构造入参数组不影响内部值（双重防御性拷贝）。
  - 单参构造 getAlgorithm 为 null；2 参构造 getAlgorithm 为传入算法名。
  - `instanceof EncodedKeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：ctor null→NPE
- 关键实测：EncodedKeySpec 子类，getFormat 恒 "PKCS#8"；不解析内容（任意字节可存）。JDK-9+ 增加 (byte[],String) 构造器并实现 java.security.BinaryEncodable（预览标记）。
KeyFactory.generatePrivate 消费；PrivateKey.getEncoded() 产出。
