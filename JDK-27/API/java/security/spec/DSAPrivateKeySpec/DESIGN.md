# java.security.spec.DSAPrivateKeySpec

## 1. API 功能
持有 DSA 私钥值 `x` 及算法参数 `p`、`q`、`g`，实现 `KeySpec`，供 `KeyFactory.generatePrivate` 还原私钥对象。

## 2. Java 用户如何使用
用户构造 `new DSAPrivateKeySpec(x, p, q, g)`，再调用 `KeyFactory.getInstance("DSA").generatePrivate(spec)` 得到 `PrivateKey`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/provider/DSA/TestDSA.java:115-117`：
  ```java
  DSAPrivateKeySpec privSpec = new DSAPrivateKeySpec(x, p, q, g);
  PrivateKey privateKey = kf.generatePrivate(privSpec);
  ```
- `test/jdk/sun/security/pkcs11/Signature/TestDSA.java` 同样使用 `DSAPrivateKeySpec` 构造 pkcs11 DSA 私钥。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `java.security.spec.KeySpec`，方法 `getX/getP/getQ/getG` 存在。
- `testBlackBox()`：固定四元组 `(1,2,3,4)` 校验四个 getter；断言 `instanceof KeySpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：getter 同引用
- 关键实测：4 参构造（全 null 合法），实现 KeySpec；无 equals 覆盖、非 Serializable。
KeyFactory("DSA").getKeySpec(priv, DSAPrivateKeySpec.class) 产出；generatePrivate 消费。
