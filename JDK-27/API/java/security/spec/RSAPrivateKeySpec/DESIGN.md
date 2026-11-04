# java.security.spec.RSAPrivateKeySpec 测试设计

## 1. API 功能
`RSAPrivateKeySpec` 实现 `KeySpec`，描述 RSA 私钥的模数与私钥指数。提供 2 参构造器（modulus, privateExponent）与可附加 AlgorithmParameterSpec 的 3 参构造器；getModulus/getPrivateExponent/getParams 回读。

## 2. Java 用户如何使用
```java
RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, privateExponent);
PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(spec);
```

## 3. 开源代码实际用法调研
- `test/jdk/com/sun/crypto/provider/Cipher/RSA/TestRSA.java`:
  ```java
  RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(N, D);
  PrivateKey privateKey = kf.generatePrivate(privSpec);
  ```
- `test/jdk/java/security/KeyFactory/KeyFactoryGetKeySpecForInvalidSpec.java`（getKeySpec 返回该类型）。
- JDK 源码 `RSAPrivateKeySpec.java` 中 getParams：
  ```java
  public AlgorithmParameterSpec getParams() { return this.params; }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 KeySpec、含 getModulus/getPrivateExponent/getParams 方法。
- 黑盒（testBlackBox）：
  - 2 参构造：getModulus/getPrivateExponent 精确、getParams 为 null。
  - 3 参构造：三个 getter 精确、getParams 为传入对象。
  - `instanceof KeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：同引用
- 关键实测：2/3 参构造；getter 原引用；getParams 为 JDK 后加通道；无 equals 覆盖。
KeyFactory("RSA") 私钥交换基类（RSAPrivateCrtKeySpec 继承）。
