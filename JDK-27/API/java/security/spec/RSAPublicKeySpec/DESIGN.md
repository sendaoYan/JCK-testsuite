# java.security.spec.RSAPublicKeySpec 测试设计

## 1. API 功能
`RSAPublicKeySpec` 实现 `KeySpec`，描述 RSA 公钥的模数与公钥指数。提供 2 参构造器（modulus, publicExponent）与可附加 AlgorithmParameterSpec 的 3 参构造器；getModulus/getPublicExponent/getParams 回读。

## 2. Java 用户如何使用
```java
RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
PublicKey key = KeyFactory.getInstance("RSA").generatePublic(spec);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/crypto/CryptoPermission/RSANoLimit.java`:
  ```java
  RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec
      (new BigInteger(MODULUS4096), new BigInteger(PUB4096));
  KeyFactory kf = KeyFactory.getInstance(algo);
  RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(pubKeySpec);
  ```
- `test/jdk/com/sun/crypto/provider/Cipher/RSA/TestRSA.java`（同样用该规范构造公钥）。
- JDK 源码 `RSAPublicKeySpec.java` 中 getParams：
  ```java
  public AlgorithmParameterSpec getParams() { return this.params; }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 KeySpec、含 getModulus/getPublicExponent/getParams 方法。
- 黑盒（testBlackBox）：
  - 2 参构造：getModulus/getPublicExponent 精确、getParams 为 null。
  - 3 参构造：三个 getter 精确、getParams 为传入对象。
  - `instanceof KeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：同引用
- 关键实测：镜像私钥侧；与 RSAPrivateKeySpec 为兄弟类（互不赋值）。
KeyFactory("RSA") 公钥交换。
