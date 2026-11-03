# java.security.spec.DSAParameterSpec

## 1. API 功能
持有 DSA 参数 `p`（素数模）、`q`（子群素数）、`g`（生成元）三个大整数，供 `KeyFactory` 生成 DSA 密钥或描述算法参数。同时实现 `AlgorithmParameterSpec` 与 `java.security.interfaces.DSAParams`。

## 2. Java 用户如何使用
用户用三个 `BigInteger` 构造该对象，传给 `KeyFactory.generatePublic/generatePrivate`（配合 `DSAPublicKeySpec`/`DSAPrivateKeySpec`），或作为算法参数传给 `AlgorithmParameters`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/IsAssignableFromOrder.java:70`：
  ```java
  testAlgSpec("DSA", new DSAParameterSpec(BigInteger.ONE, BigInteger.TWO, BigInteger.TEN));
  ```
- `test/jdk/sun/security/provider/DSA/TestDSA.java:115-116`（与 KeySpec 配合）：
  ```java
  DSAPrivateKeySpec privSpec = new DSAPrivateKeySpec(x, p, q, g);
  DSAPublicKeySpec pubSpec = new DSAPublicKeySpec(y, p, q, g);
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、同时实现 `AlgorithmParameterSpec` 与 `java.security.interfaces.DSAParams`，方法 `getP/getQ/getG` 存在。
- `testBlackBox()`：用固定 `p=23,q=11,g=5` 校验三个 getter；断言 `instanceof AlgorithmParameterSpec` 与 `instanceof DSAParams`。
