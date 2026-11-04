# java.security.KeyFactorySpi 测试设计（JDK-8）

## 1. API 功能
- 抽象引擎类，`KeyFactory` 外观的 SPI 后端；public 无参构造。
- 4 个抽象方法：`engineGeneratePublic(KeySpec)`、`engineGeneratePrivate(KeySpec)`、`engineGetKeySpec(Key,Class)`、`engineTranslateKey(Key)`，全为 protected abstract。

## 2. Java 用户如何使用
provider 继承 KeyFactorySpi，实现 X.509/PKCS#8 规格与 Key 对象互转，注册后由 `KeyFactory.getInstance("DSA")` 装配。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/KeyFactorySpi.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/KeyFactory/`
- DSA 实现 `sun.security.provider.DSAKeyFactory`

## 4. 测试用例设计 + 自动化实现
- 结构：exact-4-abstract 检查（名字集合精确），全 protected。
- 透明记录子类：`engineGeneratePublic(spec)`/`engineGetKeySpec(k,cls)` 记录并返回标记对象，断言实参同一（spec/key/cls 均同一实例）；`engineTranslateKey` 同。
- 外观闭环（真实 DSA SPI）：`generatePrivate(getKeySpec(pub,X509))` 反向往返 equals；`translateKey(pub)` equals。
- 外观异常矩阵：`generatePublic(null)` → `InvalidKeySpecException`；`getKeySpec(null,cls)` → IKSE；`getKeySpec(k,null)` → **NPE**；`translateKey(null)` → `InvalidKeyException`；`generatePublic(PKCS8EncodedKeySpec)` 喂 DSA 公钥工厂 → IKSE。

断言入口：`KeyFactorySpiTest.testContractDeep()`（7→32 断言）。
