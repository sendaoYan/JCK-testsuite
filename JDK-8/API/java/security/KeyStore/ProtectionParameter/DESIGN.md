# java.security.KeyStore.ProtectionParameter 黑盒行为测试设计

## 1. API 功能
保护参数标记接口：0 个自有方法。两个 JDK 内置实现 PasswordProtection（口令数组）与 CallbackHandlerProtection（回调）；DKS 的 DomainLoadStoreParameter 组合条目级保护 map。

## 2. 使用方式
作为 `setEntry/getEntry/Builder.newInstance` 的 protection 形参类型；实现类自带具体口令获取方式。

## 3. 参考来源
- javadoc `java.security.KeyStore.ProtectionParameter`。
- OpenJDK `test/jdk/java/security/KeyStore/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 结构：0 个声明方法（纯标记）；匿名实现合法且 identity 相等；数组协变 `ProtectionParameter[]` 可装两种实现。
- 语义校验：PrivateKeyEntry setEntry 传 null protection → KeyStoreException("non-null password required to create PrivateKeyEntry")。
- PasswordProtection 经 Destroyable 引用调用 destroy 生效（isDestroyed true）。
- DomainLoadStoreParameter 的 protection map 以 ProtectionParameter 为值类型。
- 易错点：null protection 对 TrustedCertificateEntry 是唯一合法值，对 key 条目则被拒——方向不能搞反。

断言入口：`ProtectionParameterTest.java` 的 `testContractDeep()`（原有结构断言保留）。
- 补充：自定义匿名 ProtectionParameter 取 key 条目 → UOE；DLSP 快照 + 不可修改视图；destroy 幂等。
