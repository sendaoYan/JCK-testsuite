# java.security.spec.ECGenParameterSpec

## 1. API 功能
按标准曲线名（如 "secp256r1"、"prime192v1"）描述 EC 算法参数。继承自 `NamedParameterSpec`（其实现 `AlgorithmParameterSpec`），`getName()` 为继承方法。

## 2. Java 用户如何使用
用户构造 `new ECGenParameterSpec("secp256r1")` 后传给 `KeyPairGenerator.initialize(...)` 或 `AlgorithmParameters.init(...)`，由 JDK 按名称解析曲线参数。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/IsAssignableFromOrder.java:73`：
  ```java
  testAlgSpec("EC", new ECGenParameterSpec("secp256r1"));
  ```
- `test/jdk/java/security/spec/ECCBasic.java:55-56`：
  ```java
  private static final ECGenParameterSpec GENPARAMS =
      new ECGenParameterSpec("prime192v1");
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类为 `NamedParameterSpec`，且继承方法 `getName` 可通过 `getMethod` 取得。
- `testBlackBox()`：`new ECGenParameterSpec("secp256r1")` 校验 `getName()`；断言 `instanceof NamedParameterSpec` 与 `instanceof AlgorithmParameterSpec`。
