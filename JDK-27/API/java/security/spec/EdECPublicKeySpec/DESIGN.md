# java.security.spec.EdECPublicKeySpec

## 1. API 功能
RFC 8032 EdDSA 公钥规范：`NamedParameterSpec params`（曲线/参数名）与 `EdECPoint point`（公钥点）。`final` 类，实现 `KeySpec`。

## 2. Java 用户如何使用
用户构造 `new EdECPublicKeySpec(NamedParameterSpec.ED25519, point)`，再调用 `KeyFactory.getInstance("EdDSA").generatePublic(spec)` 得到 `PublicKey`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/ec/ed/EdDSATest.java:175`：
  ```java
  kf.generatePublic(new EdECPublicKeySpec(namedSpec,
      ((EdECPublicKey) key).getPoint()))
  ```
- `src/java.base/share/classes/java/security/spec/EdECPublicKeySpec.java` 构造器校验 `params/point` 非空并保存引用。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、`final`、非接口、父类 `Object`、实现 `java.security.spec.KeySpec`，方法 `getParams/getPoint` 存在。
- `testBlackBox()`：`NamedParameterSpec.ED25519` + `EdECPoint(false, 10)` 校验 `getParams`、`getPoint`；断言 `instanceof KeySpec`。
