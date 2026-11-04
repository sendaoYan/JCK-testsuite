# java.security.spec.AlgorithmParameterSpec

## 1. API 功能
一个空标记接口（marker interface），不含任何方法，用于标识“算法参数规范”这一族类型。所有算法参数规范类（如 `DSAParameterSpec`、`ECParameterSpec`、`EdDSAParameterSpec`）都实现它，从而可被 `KeyPairGenerator.initialize`、`AlgorithmParameters.init`、`Signature.setParameter` 等接受为通用参数类型。

## 2. Java 用户如何使用
用户不直接实例化该接口，而是使用具体实现类，并把它作为参数传给安全 API。类型上以 `AlgorithmParameterSpec` 作为入参，实现多态分发。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/IsAssignableFromOrder.java:93`：
  ```java
  static void testAlgSpec(String algorithm, AlgorithmParameterSpec spec,
          Class<? extends AlgorithmParameterSpec>... classes)
  ```
- 同上 `IsAssignableFromOrder.java:70,73`：
  ```java
  testAlgSpec("DSA", new DSAParameterSpec(BigInteger.ONE, BigInteger.TWO, BigInteger.TEN));
  testAlgSpec("EC", new ECGenParameterSpec("secp256r1"));
  ```
- 实现侧（JDK 源码）：`java/security/spec/DSAParameterSpec.java`、`ECParameterSpec.java`、`EdDSAParameterSpec.java` 均 `implements AlgorithmParameterSpec`。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `public`、`isInterface()`、无父类（`getSuperclass()==null`）、声明方法数为 0（纯标记接口）。
- `testBlackBox()`：断言本组具体类 `DSAParameterSpec` 实例 `instanceof AlgorithmParameterSpec`，验证接口确实被具体参数规范类实现。
