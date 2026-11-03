# java.security.spec.EdDSAParameterSpec

## 1. API 功能
描述 RFC 8032 EdDSA 签名/验证参数：`prehash` 模式与可选 `context`。构造 `(boolean prehash)` 无上下文（context 为 null，对应 `Optional.empty()`）；构造 `(boolean prehash, byte[] context)` 复制上下文（长度 ≤ 255，否则抛 `InvalidParameterException`）。`getContext()` 返回 `Optional<byte[]>`，每次都返回新副本。

## 2. Java 用户如何使用
用户构造该对象后通过 `Signature.setParameter(...)` 指定 Ed25519/Ed448 的 prehash 或 context 模式；`getContext()` 用于校验上下文。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/ec/ed/EdDSANegativeTest.java:124-126`：
  ```java
  EdDSAParameterSpec initParam
          = new EdDSAParameterSpec(true, "testContext".getBytes());
  sig.setParameter(initParam);
  ```
- `test/jdk/sun/security/ec/ed/EdDSATest.java:202,207`：
  ```java
  new EdDSAParameterSpec(preHash)
  new EdDSAParameterSpec(preHash, context)
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`、实现 `AlgorithmParameterSpec`，方法 `isPrehash/getContext` 存在。
- `testBlackBox()`：无上下文构造校验 `isPrehash==false` 且 `getContext()` 为空；带上下文 `{1,2,3}` 校验 `isPrehash==true`、上下文内容相等；输入数组变异不影响存储副本，`getContext()` 返回的新数组变异后重取不变；断言 `instanceof AlgorithmParameterSpec`。
