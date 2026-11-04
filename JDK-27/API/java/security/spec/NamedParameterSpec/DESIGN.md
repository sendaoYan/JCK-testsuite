# java.security.spec.NamedParameterSpec 测试设计

## 1. API 功能
`NamedParameterSpec` 实现 `AlgorithmParameterSpec`，用标准名字标识一组预定义算法参数（如 X25519、Ed25519、ML-KEM-512 等）。提供 10 个静态常量、`String` 构造器与 getName。

## 2. Java 用户如何使用
```java
XECPublicKeySpec pub = new XECPublicKeySpec(NamedParameterSpec.X25519, u);
String name = NamedParameterSpec.X25519.getName();  // "X25519"
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/TestNamedParameterSpec.java`（校验常量名集合）:
  ```java
  private static String[] EXPECTED = {
      "ED25519", "ED448", "X25519", "X448",
      "ML_DSA_44", "ML_DSA_65", "ML_DSA_87",
      "ML_KEM_512", "ML_KEM_768", "ML_KEM_1024",
  };
  ```
- `test/jdk/java/security/KeyAgreement/KeySpecTest.java`（通过 getName 比较曲线名）:
  ```java
  if (((NamedParameterSpec)((XECPrivateKeySpec) priSpec).getParams()).getName()
      != ((NamedParameterSpec)((XECPublicKeySpec) pubSpec).getParams()).getName()) {
      throw new RuntimeException("Curve name mismatch found");
  }
  ```
- JDK 源码 `NamedParameterSpec.java` 中常量定义：
  ```java
  public static final NamedParameterSpec X25519 = new NamedParameterSpec("X25519");
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 AlgorithmParameterSpec、含 getName 方法。
- 黑盒（testBlackBox）：
  - `String` 构造 getName 精确回读。
  - 10 个常量 getName 逐一精确断言（X25519/X448/Ed25519/Ed448/ML-DSA-*/ML-KEM-*）。
  - `instanceof AlgorithmParameterSpec`。
