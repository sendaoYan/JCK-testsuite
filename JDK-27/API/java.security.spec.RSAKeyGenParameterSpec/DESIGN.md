# java.security.spec.RSAKeyGenParameterSpec 测试设计

## 1. API 功能
`RSAKeyGenParameterSpec` 实现 `AlgorithmParameterSpec`，描述 RSA 密钥对生成的参数：密钥长度与公钥指数。提供常量 `F0`（=3）与 `F4`（=65537），以及 2 参构造器（keysize, publicExponent）和可附带额外算法参数的 3 参构造器。getKeysize/getPublicExponent/getKeyParams 分别回读。

## 2. Java 用户如何使用
```java
KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
kpg.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
```

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/tools/keytool/fakegen/java.base/sun/security/rsa/RSAKeyPairGenerator.java`:
  ```java
  initialize(new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4), random);
  ```
- `test/jdk/java/security/KeyPairGenerator/GenerateRSAKeyPair.java`（使用 RSAKeyGenParameterSpec 初始化）。
- JDK 源码 `RSAKeyGenParameterSpec.java` 中常量定义：
  ```java
  public static final BigInteger F0 = BigInteger.valueOf(3);
  public static final BigInteger F4 = BigInteger.valueOf(65537);
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 AlgorithmParameterSpec、含 getKeysize/getPublicExponent/getKeyParams 方法。
- 黑盒（testBlackBox）：
  - F0 == 3、F4 == 65537。
  - 2 参构造：getKeysize/getPublicExponent 精确、getKeyParams 为 null。
  - 3 参构造：getKeysize/getPublicExponent 精确、getKeyParams 为传入对象。
  - `instanceof AlgorithmParameterSpec`。
