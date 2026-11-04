# java.security.spec.DSAPublicKeySpec

## 1. API 功能
持有 DSA 公钥值 `y` 及算法参数 `p`、`q`、`g`，实现 `KeySpec`，供 `KeyFactory.generatePublic` 还原公钥对象。

## 2. Java 用户如何使用
用户构造 `new DSAPublicKeySpec(y, p, q, g)`，再调用 `KeyFactory.getInstance("DSA").generatePublic(spec)` 得到 `PublicKey`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/provider/DSA/TestDSA.java:116-118`：
  ```java
  DSAPublicKeySpec pubSpec = new DSAPublicKeySpec(y, p, q, g);
  PublicKey publicKey = kf.generatePublic(pubSpec);
  ```
- `test/jdk/javax/xml/crypto/dsig/GenerationTests.java` 使用 `DSAPublicKeySpec` 参与 XML 数字签名的密钥还原。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `java.security.spec.KeySpec`，方法 `getY/getP/getQ/getG` 存在。
- `testBlackBox()`：固定四元组 `(1,2,3,4)` 校验四个 getter；断言 `instanceof KeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：镜像私钥侧 + generatePublic equals 闭环。
- 关键实测：同 DSAPrivateKeySpec 结构，y 为公钥分量。
KeyFactory("DSA") getKeySpec/generatePublic 往返。
