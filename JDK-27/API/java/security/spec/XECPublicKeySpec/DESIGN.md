# java.security.spec.XECPublicKeySpec 测试设计

## 1. API 功能
`XECPublicKeySpec` 实现 `KeySpec`，描述 XDH/X25519/X448 公钥：算法参数（通常为 NamedParameterSpec）与 u 坐标（BigInteger）。getParams/getU 回读。

## 2. Java 用户如何使用
```java
XECPublicKeySpec spec = new XECPublicKeySpec(NamedParameterSpec.X25519, u);
BigInteger u = spec.getU();
NamedParameterSpec params = (NamedParameterSpec) spec.getParams();
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyAgreement/KeySpecTest.java`:
  ```java
  return Arrays.asList(X509EncodedKeySpec.class, XECPublicKeySpec.class);
  ...
  if (priSpec instanceof XECPrivateKeySpec && pubSpec instanceof XECPublicKeySpec) {
      if (((NamedParameterSpec) ((XECPrivateKeySpec) priSpec).getParams()).getName()
          != ((NamedParameterSpec) ((XECPublicKeySpec) pubSpec).getParams()).getName()) {
          throw new RuntimeException("Curve name mismatch found");
      }
  }
  ```
- `test/jdk/sun/security/ec/xec/TestXDH.java`（XDH 公钥规范类型测试）。
- JDK 源码 `XECPublicKeySpec.java` 中构造与 getU：
  ```java
  this.u = u;
  ...
  public BigInteger getU() { return u; }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 KeySpec、含 getParams/getU 方法。
- 黑盒（testBlackBox）：getParams 返回传入的 NamedParameterSpec.X25519、getU 精确返回传入的 BigInteger、`instanceof KeySpec`。
