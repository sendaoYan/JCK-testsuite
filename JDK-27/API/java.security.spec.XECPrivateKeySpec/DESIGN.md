# java.security.spec.XECPrivateKeySpec 测试设计

## 1. API 功能
`XECPrivateKeySpec` 实现 `KeySpec`，描述 XDH/X25519/X448 私钥：算法参数（通常为 NamedParameterSpec）与标量字节数组。构造器对 scalar 做防御性拷贝；getScalar 每次返回新数组；getParams 返回参数对象。

## 2. Java 用户如何使用
```java
XECPrivateKeySpec spec = new XECPrivateKeySpec(NamedParameterSpec.X25519, scalar);
byte[] s = spec.getScalar();          // 防御性拷贝
NamedParameterSpec params = (NamedParameterSpec) spec.getParams();
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyAgreement/KeySpecTest.java`:
  ```java
  return Arrays.asList(PKCS8EncodedKeySpec.class, XECPrivateKeySpec.class);
  ...
  if (priSpec instanceof XECPrivateKeySpec && pubSpec instanceof XECPublicKeySpec) {
      if (((NamedParameterSpec) ((XECPrivateKeySpec) priSpec).getParams()).getName() ...)
  }
  ```
- `test/jdk/sun/security/ec/xec/TestXDH.java`（XDH 密钥规范类型测试）。
- JDK 源码 `XECPrivateKeySpec.java` 中构造与 getScalar：
  ```java
  this.scalar = scalar.clone();
  ...
  public byte[] getScalar() { return scalar.clone(); }
  ```

## 4. 测试用例设计 + 自动化实现
- 白盒（testWhiteBox）：public 具体类、实现 KeySpec、含 getParams/getScalar 方法。
- 黑盒（testBlackBox）：
  - getParams 返回传入的 NamedParameterSpec.X25519。
  - getScalar 返回内容相等的新数组；修改返回数组不影响后续取值；修改构造入参数组不影响内部值（双重防御性拷贝）。
  - `instanceof KeySpec`。
