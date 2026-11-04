# java.security.CryptoPrimitive

## 1. API 功能
枚举类型，列出密码学原语类别：`MESSAGE_DIGEST`、`SECURE_RANDOM`、`BLOCK_CIPHER`、`STREAM_CIPHER`、`MAC`、`KEY_WRAP`、`PUBLIC_KEY_ENCRYPTION`、`SIGNATURE`、`KEY_ENCAPSULATION`、`KEY_AGREEMENT`。主要用于算法约束/禁用算法检查（`AlgorithmConstraints`）。

## 2. Java 用户如何使用
用户用 `EnumSet.of(CryptoPrimitive.SIGNATURE)` 等方式构造原语集合，传给 `AlgorithmConstraints.permits(Set<CryptoPrimitive>, ...)` 判断某算法是否被允许。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/CryptoPrimitive.java`（JDK 源）：`public enum CryptoPrimitive { MESSAGE_DIGEST, SECURE_RANDOM, BLOCK_CIPHER, STREAM_CIPHER, MAC, KEY_WRAP, PUBLIC_KEY_ENCRYPTION, SIGNATURE, KEY_ENCAPSULATION, KEY_AGREEMENT }`
- `test/jdk/sun/security/tools/keytool/WeakAlg.java`：
  - `Set<CryptoPrimitive> sigPrimitiveSet = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public enum、超类为 `Enum`、含 10 个常量、含 `valueOf`/`values` 方法。
- `testBlackBox()`：断言 `valueOf("SIGNATURE")==SIGNATURE`、`values().length==10`，并对每个常量做 `valueOf(name())` 往返一致。
