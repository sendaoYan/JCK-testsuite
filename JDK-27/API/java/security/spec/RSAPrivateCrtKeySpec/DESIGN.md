# java.security.spec.RSAPrivateCrtKeySpec 测试设计

## 1. API 功能
`RSAPrivateCrtKeySpec` 继承 `RSAPrivateKeySpec`，描述 CRT 形式的 RSA 私钥：模数、公钥指数、私钥指数、素数 p/q、CRT 指数 dP/dQ 与 CRT 系数。提供 8 参构造器与可附加 AlgorithmParameterSpec 的 9 参构造器；继承的 getModulus/getPrivateExponent/getParams 与新增的 6 个 CRT getter 回读各分量。

## 2. Java 用户如何使用
```java
RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(
    modulus, publicExp, privateExp, p, q, expP, expQ, crtCoeff);
BigInteger mod = spec.getModulus();
BigInteger p = spec.getPrimeP();
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/GenerateRSAPrivateCrtKey.java`:
  ```java
  RSAPrivateCrtKeySpec rsaCrtSpec =
      new RSAPrivateCrtKeySpec(new BigInteger(1, modulus),
                               new BigInteger(1, pubExpo),
                               new BigInteger(1, priExpo),
                               new BigInteger(1, primeP),
                               new BigInteger(1, primeQ),
                               new BigInteger(1, expoP),
                               new BigInteger(1, expoQ), ...);
  ```
- `test/jdk/java/security/KeyFactory/KeyFactoryGetKeySpecForInvalidSpec.java`（getKeySpec 返回该类型）。
- JDK 源码 `RSAPrivateCrtKeySpec.java` 中 9 参构造委托 `super(modulus, privateExponent, keyParams)`。

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、父类 RSAPrivateKeySpec、含 6 个 CRT getter 方法。
- 黑盒（testBlackBox）：
  - 8 参构造：8 个 getter（含继承 getModulus/getPrivateExponent）精确断言、getParams 为 null。
  - 9 参构造：getParams 为传入对象、getPrimeP 精确。
