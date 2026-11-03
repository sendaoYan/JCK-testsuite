# java.security.spec.EdECPrivateKeySpec

## 1. API 功能
RFC 8032 EdDSA 私钥规范：`NamedParameterSpec params`（曲线/参数名）与字节数组 `bytes`（私钥，长度为 8 的倍数）。构造时复制输入数组，`getBytes()` 每次返回新副本。`final` 类，实现 `KeySpec`。

## 2. Java 用户如何使用
用户构造 `new EdECPrivateKeySpec(NamedParameterSpec.ED25519, keyBytes)`，再调用 `KeyFactory.getInstance("EdDSA").generatePrivate(spec)` 得到 `PrivateKey`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/ec/ed/EdDSATest.java:183-184`：
  ```java
  kf.generatePrivate(new EdECPrivateKeySpec(namedSpec,
      ((EdECPrivateKey) key).getBytes().get()))
  ```
- 实现侧 `src/java.base/share/classes/java/security/spec/EdECPrivateKeySpec.java` 构造器与 `getBytes()` 均使用 `bytes.clone()`。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、`final`、非接口、父类 `Object`、实现 `java.security.spec.KeySpec`，方法 `getParams/getBytes` 存在。
- `testBlackBox()`：`NamedParameterSpec.ED25519` + 32 字节数组校验 `getParams` 与 `getBytes`；输入数组变异后存储不变，返回数组变异后重取不变；断言 `instanceof KeySpec`。
