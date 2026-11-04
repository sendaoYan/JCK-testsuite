# java.security.KeyStore.PasswordProtection 黑盒行为测试设计

## 1. API 功能
ProtectionParameter + Destroyable 实现：`getPassword()`、`destroy()`、`isDestroyed()`；构造器（char[]）与（char[], String protectionAlgorithm, AlgorithmParameterSpec protectionParameters）。

## 2. 使用方式
`new KeyStore.PasswordProtection(pw)` → `ks.setEntry(alias, entry, pp)` → 同一 pp 再 `getEntry`；用完 destroy 抹除口令。

## 3. 参考来源
- javadoc `java.security.KeyStore.PasswordProtection`。
- OpenJDK `test/jdk/java/security/KeyStore/PasswordProtection/`。
- JCK runtime-8b `api/java_security/KeyStore/`。

## 4. 测试用例设计 + 易错规范细节
- 防御性拷贝：构造后改源数组不影响内部；`getPassword()` 返回内部数组（两次调用 `==`，读侧不拷贝——与 PasswordCallback 的克隆语义相反，易混）。
- destroy 生命周期：destroy → isDestroyed true；destroy 后 getPassword → IllegalStateException；destroy 幂等。
- null/空口令均合法（JCEKS 空口令可写入）。
- 3 参构造：null algorithm → NPE("invalid null input")；"PBE" + null spec 合法且 getter 往返。
- 端到端：JCEKS setEntry/getEntry 往返 + 错口令 → UnrecoverableKeyException。
- 易错点：PasswordProtection 无 equals/hashCode 覆写（identity 相等）；getProtectionAlgorithm 仅有 3 参构造版本才有值。

断言入口：`PasswordProtectionTest.java` 的 `testContractDeep()`（原有结构断言保留）。
