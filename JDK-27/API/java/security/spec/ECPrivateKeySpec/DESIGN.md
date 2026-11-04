# java.security.spec.ECPrivateKeySpec

## 1. API 功能
持有 EC 私钥标量 `s` 与域参数 `ECParameterSpec params`，实现 `KeySpec`，供 `KeyFactory.generatePrivate` 还原 EC 私钥。

## 2. Java 用户如何使用
用户构造 `new ECPrivateKeySpec(s, params)`，再调用 `KeyFactory.getInstance("EC").generatePrivate(spec)` 得到 `PrivateKey`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/ECCBasic.java:57-58`：
  ```java
  private static final ECPrivateKeySpec PRIV_KEY = new ECPrivateKeySpec
      (TEN, PARAMS);
  ```
- `test/jdk/javax/xml/crypto/dsig/GenerationTests.java` 使用 `ECPrivateKeySpec` 参与 XML 签名密钥还原。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `java.security.spec.KeySpec`，方法 `getS/getParams` 存在。
- `testBlackBox()`：构造 p=23 域参数与 `s=5`，校验 `getS`、`getParams` 返回传入对象；断言 `instanceof KeySpec`。
