# java.security.spec.ECPublicKeySpec

## 1. API 功能
持有 EC 公钥点 `w`（`ECPoint`）与域参数 `ECParameterSpec params`，实现 `KeySpec`，供 `KeyFactory.generatePublic` 还原 EC 公钥。

## 2. Java 用户如何使用
用户构造 `new ECPublicKeySpec(w, params)`，再调用 `KeyFactory.getInstance("EC").generatePublic(spec)` 得到 `PublicKey`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/ECCBasic.java:59-60`：
  ```java
  private static final ECPublicKeySpec PUB_KEY = new ECPublicKeySpec
      (POINT, PARAMS);
  ```
- `test/jdk/sun/security/pkcs11/ec/TestKeyFactory.java` 使用 `ECPublicKeySpec` 构造并验证 EC 公钥。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `java.security.spec.KeySpec`，方法 `getW/getParams` 存在。
- `testBlackBox()`：构造 p=23 域参数与公钥点 `w=(1,2)`，校验 `getW`、`getParams` 返回传入对象；断言 `instanceof KeySpec`。
