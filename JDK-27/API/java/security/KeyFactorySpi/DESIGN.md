# java.security.KeyFactorySpi

## 1. API 功能
密钥工厂的服务提供者接口。实现者需提供四个抽象引擎方法：`engineGeneratePublic(KeySpec)`、`engineGeneratePrivate(KeySpec)`、`engineGetKeySpec(Key, Class<T>)`、`engineTranslateKey(Key)`，用于在 `Key` 与 `KeySpec`（密钥规范）之间转换。

## 2. Java 用户如何使用
用户通过 `KeyFactory.getInstance("DSA")` 获取封装实例，`generatePublic`/`generatePrivate`/`getKeySpec`/`translateKey` 最终委托给 SPI。提供者子类实现具体的编码转换逻辑。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/KeyFactorySpi.java`（JDK 源）：声明四个 abstract 引擎方法。
- `test/jdk/java/security/KeyFactory/Failover.java`：
  - `public static class KeyFactoryPass extends KeyFactorySpi { protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {...} protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {...} protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {...} protected Key engineTranslateKey(Key key) throws InvalidKeyException {...} }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言为 public abstract class、超类 `Object`，含四个 `engine*` 方法。
- `testBlackBox()`：最小子类 `MyKfSpi` 返回 `DummyPublicKey`/`DummyPrivateKey`（algorithm "Dummy"），`engineTranslateKey` 原样返回；断言 `engineGeneratePublic`/`engineGeneratePrivate` 返回非空且算法为 "Dummy"，`engineTranslateKey` 返回同一对象。

## 5. 契约深化（testContractDeep，本轮新增）
- exact-4-abstract；记录子类四方法精确透传（spec/key/Class 同一实例）。
- 真实 DSA KeyFactory 外观闭环：X.509/PKCS#8 规格往返 equals、translateKey(pub) equals。
- 异常矩阵：generatePublic(null)→InvalidKeySpecException、getKeySpec(null,cls)→IKSE、getKeySpec(k,null)→NPE、translateKey(null)→InvalidKeyException、PKCS8 规格喂公钥工厂→IKSE。
