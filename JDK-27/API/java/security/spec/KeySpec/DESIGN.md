# java.security.spec.KeySpec 测试设计

## 1. API 功能
`KeySpec` 是（透明）密钥规范的标记接口。它不声明任何方法，仅作为一个类型标记，让不同的密钥规范类（如 `RSAPublicKeySpec`、`X509EncodedKeySpec`）都能被 `KeyFactory.getKeySpec(...)` 等 API 统一接受。

## 2. Java 用户如何使用
用户不直接实现该接口，而是使用具体规范类；`KeySpec` 主要作为 API 参数/返回类型出现：
```java
KeySpec spec = keyFactory.getKeySpec(key, RSAPublicKeySpec.class);
if (spec instanceof RSAPublicKeySpec rsa) {
    BigInteger modulus = rsa.getModulus();
}
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/Failover.java`（自定义 provider 的 SPI 以 KeySpec 为参数）:
  ```java
  protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException { ... }
  protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException { ... }
  ```
- `test/jdk/java/security/KeyAgreement/KeySpecTest.java`（import KeySpec 作为规范列表元素类型）。
- JDK 源码 `java.security.KeyFactory.java` 中 `getKeySpec(Key key, Class<T> keySpec)` 使用 KeySpec 作为泛型上界。

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 接口、无父接口（getSuperclass()==null）、不声明任何方法。
- 黑盒（testBlackBox）：真实规范类 `RSAPublicKeySpec`、`RSAPrivateKeySpec` 实例均 `instanceof KeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：标记语义验证（instanceof/isAssignableFrom 矩阵覆盖两族、与 AlgorithmParameterSpec 体系互斥、null 可赋值）。
- 关键实测：所有 *KeySpec 类型的公共标记接口；KeyFactory.generatePublic/generatePrivate/getKeySpec 以本接口体系交换密钥材料。
实现分两大族：EncodedKeySpec（PKCS8/X509 编码字节）与算法专有 spec（DSA*/RSA*/EC*）。
